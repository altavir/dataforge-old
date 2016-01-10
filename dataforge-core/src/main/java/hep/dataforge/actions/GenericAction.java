/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.actions;

import hep.dataforge.meta.Meta;
import hep.dataforge.content.Content;
import hep.dataforge.content.Named;
import hep.dataforge.context.Context;
import hep.dataforge.dependencies.Dependency;
import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.dependencies.GenericDependency;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ActionDescriptor;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.log.Log;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.content.NamedMetaHolder;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A basic implementation of Action interface
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 * @param <T>
 * @param <R>
 */
public abstract class GenericAction<T extends Named, R extends Named> extends NamedMetaHolder implements Action<T, R> {

    public static final String ACTION_DEPENDENCY_LOG_KEY = "log";
    public static final String ACTION_DEPENDENCY_META_KEY = "meta";

    private final Context context;
    
    private List<ActionStateListener> listeners = new ArrayList<>();

    /**
     * <p>
     * Constructor for GenericAction.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public GenericAction(Context context, String name, Meta annotation) {
        super(name, annotation);
        this.context = context;
    }

    public GenericAction(Context context, Meta annotation) {
        super(annotation);
        this.context = context;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Context getContext() {
        return context;
    }

    public ActionResult<R> run(Meta config, Collection<Dependency<T>> data) {
        return run(new Pack<>(null, config, buildLog(config), getInputType(), data));
    }

    protected Logable buildLog(Meta config) {
        //TODO add log builder from annotation
        return getContext();
    }

    protected boolean isParallelExecutionAllowed() {
        return meta().getBoolean("action.allowParallel", true);
    }

    /**
     * Wrap result of single execution to present it as a dependency
     *
     * @param singleLog an individual log for this result. Could be null;
     * @param singleMeta an individual meta for this result. Could be null;
     * @param singleResult
     * @return
     */
    protected Dependency<R> wrap(Logable singleLog, Meta singleMeta, R singleResult) {
        GenericDependency.Builder<R> builder = new GenericDependency.Builder(singleResult);
        if (singleLog != null) {
            builder.putExtra(ACTION_DEPENDENCY_LOG_KEY, singleLog);
        }
        if (singleMeta != null) {
            builder.putExtra(ACTION_DEPENDENCY_META_KEY, singleMeta);
        }
        return builder.build(singleResult.getName());
    }

    protected boolean isEmptyInputAllowed() {
        return false;
    }

    /**
     * Build log for given dependency
     *
     * @param dep
     * @param parent
     * @return
     */
    protected Logable getDependencyLog(Dependency<T> dep, Logable parent) {
        if (dep.keys().contains(ACTION_DEPENDENCY_LOG_KEY)
                && dep.type(ACTION_DEPENDENCY_LOG_KEY).isAssignableFrom(Logable.class)) {
            return dep.<Logable>get(ACTION_DEPENDENCY_LOG_KEY);
        } else {
            return new Log(dep.getName(), parent);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param input
     * @return
     */
    @Override
    public ActionResult<R> run(DependencySet<T> input) {
        final Logable log;
        //= input.log() != null ? input.log() : getContext();
        if (input instanceof ActionResult) {
            ActionResult prevRes = (ActionResult) input;
            log = prevRes.log() != null ? prevRes.log() : getContext();
        } else {
            log = getContext();
        }

        beforeAction(input, log);
        if (input.isEmpty() && !isEmptyInputAllowed()) {
            log.logError("No input data in action {}", getName());
            throw new RuntimeException("No input data in action in non-generator action");
        }

        List<Dependency<R>> out = execute(log, input.meta(), input);
        ActionResult<R> res = new Pack<>(getName(), input.meta(), log, getOutputType(), out);
        afterAction(res);
        return res;
    }

    protected abstract List<Dependency<R>> execute(Logable log, Meta packAnnotation, DependencySet<T> input);

    protected void afterAction(ActionResult<R> pack) throws ContentException {
        pack.log().log("Action '{}' is finished", getName());
        for(ActionStateListener listener: listeners){
            listener.notifyActionFinished(this, pack);
        }
    }

    /**
     * <p>
     * beforeAction.</p>
     *
     * @param pack a {@link hep.dataforge.actions.ActionResult} object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    protected void beforeAction(DependencySet<T> pack, Logable log) throws ContentException {
        log.log("Starting action '{}'", getName());
        for(ActionStateListener listener: listeners){
            listener.notifyActionStarted(this, pack);
        }        
    }

    /**
     * Возвращает описание, заданное в классе. null, если описание отсутствует
     *
     * @return
     */
    private TypedActionDef getDef() {
        if (getClass().isAnnotationPresent(TypedActionDef.class)) {
            return getClass().getAnnotation(TypedActionDef.class);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public NodeDescriptor getDescriptor() {
        return ActionDescriptor.build(this);
    }

    /**
     * {@inheritDoc}
     *
     * Берет имя из описания действия. Если аннотация отсутсвует, то
     * используется имя контента
     *
     * @return
     */
    @Override
    public String getName() {
        TypedActionDef def = getDef();
        if (def != null && !def.name().isEmpty()) {
            return def.name();
        } else {
            return super.getName();
        }
    }

    /**
     * <p>
     * getInputType.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class getInputType() {
        TypedActionDef def = getDef();
        if (getDef() != null) {
            return def.inputType();
        } else {
            return Content.class;
        }
    }

    public Class getOutputType() {
        TypedActionDef def = getDef();
        if (getDef() != null) {
            return def.outputType();
        } else {
            return Content.class;
        }
    }

    protected Meta readMeta(Meta inputAnnotation) {
        return new Laminate(inputAnnotation, meta())
                .setDefaultValueProvider(getContext())
                .setDescriptor(getDescriptor());
    }

    protected Meta readMeta(Meta inputAnnotation, Meta groupAnnotation) {
        if (groupAnnotation == null) {
            return this.readMeta(inputAnnotation);
        } else {
            return new Laminate(inputAnnotation, groupAnnotation, meta())
                    .setDefaultValueProvider(getContext())
                    .setDescriptor(getDescriptor());
        }
    }

    /**
     * Create default OuputStream for given Action and given name
     *
     * @param action a {@link hep.dataforge.actions.Action} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.OutputStream} object.
     */
    public OutputStream buildActionOutput(String name) {
        return getContext().io().out(getName(), name);
    }

    /**
     * Create default OuputStream for given Action and given Content
     *
     * @param action a {@link hep.dataforge.actions.Action} object.
     * @param content a {@link hep.dataforge.content.Content} object.
     * @return a {@link java.io.OutputStream} object.
     */
    public OutputStream buildActionOutput(Named content) {
        return getContext().io().out(getName(), content.getName());
    }
    
    @Override
    public void addListener(ActionStateListener listener){
        listeners.add(listener);
    }

    
    @Override
    public void removeListener(ActionStateListener listener){
        listeners.remove(listener);
    }
}
