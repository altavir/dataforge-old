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

import hep.dataforge.content.Named;
import hep.dataforge.content.NamedMetaHolder;
import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.description.ActionDescriptor;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.io.log.Log;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import java.io.OutputStream;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataSet;
import hep.dataforge.meta.Annotated;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic implementation of Action interface
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 * @param <T>
 * @param <R>
 */
public abstract class GenericAction<T, R> extends NamedMetaHolder implements Action<T, R> {

    private final Context context;
    private Executor executor;

    public GenericAction(Context context, String name, Meta annotation) {
        super(name, annotation);
        this.context = context;
    }

    public GenericAction(Context context, Meta annotation) {
        super(annotation);
        this.context = context;
    }

    @Override
    public Context getContext() {
        if (context == null) {
            return GlobalContext.instance();
        }
        return context;
    }

    public Logger logger() {
        //TODO provide from context
        return LoggerFactory.getLogger(getClass());
    }

    protected boolean isParallelExecutionAllowed() {
        return meta().getBoolean("action.allowParallel", true);
    }

    /**
     * Wrap result of single or separate executions into DataNode
     *
     * @param singleLog an individual log for this result. Could be null;
     * @param singleMeta an individual meta for this result. Could be null;
     * @param singleResult
     * @return
     */
    protected DataNode<R> wrap(String name, Meta meta, Map<String, ? extends Data<R>> result) {
        DataSet.Builder<R> builder = DataSet.builder(getOutputType());
        result.forEach(builder::putData);
        return builder.build();
    }

    protected Log buildLog(Meta meta, Object data) {
        String logName = getName();
        if (data instanceof Named) {
            logName += "[" + ((Named) data).getName() + "]";
        }
        if (data instanceof ActionResult) {
            Log actionLog = ((ActionResult) data).log();
            if (actionLog.getParent() != null) {
                //Getting parent from previous log
                return new Log(logName, actionLog.getParent());
            } else {
                return new Log(logName, getContext());
            }
        } else {
            return new Log(logName, getContext());
        }
    }

    protected Executor buildExecutor(Meta meta, Object data) {
        if (executor == null) {
            ThreadGroup group = new ThreadGroup(getName());
            executor = Executors.newCachedThreadPool((Runnable r) -> new Thread(group, r));
        }
        return executor;
    }

    protected boolean isEmptyInputAllowed() {
        return false;
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

    public Class getInputType() {
        TypedActionDef def = getDef();
        if (getDef() != null) {
            return def.inputType();
        } else {
            return Object.class;
        }
    }

    public Class getOutputType() {
        TypedActionDef def = getDef();
        if (getDef() != null) {
            return def.outputType();
        } else {
            return Object.class;
        }
    }
    
    protected Laminate buildMeta(Data<? extends T> input, Meta nodeMeta){
        return new Laminate(input.meta(), nodeMeta, meta())
                .setValueContext(getContext())
                .setDescriptor(getDescriptor());
    }
    
    protected Laminate buildMeta(Meta nodeMeta){
        return new Laminate(nodeMeta, meta())
                .setValueContext(getContext())
                .setDescriptor(getDescriptor());
    }    
//    protected Meta readMeta(Meta inputAnnotation) {
//        return new Laminate(inputAnnotation, meta())
//                .setValueContext(getContext())
//                .setDescriptor(getDescriptor());
//    }
//
//    protected Meta readMeta(Meta inputAnnotation, Meta groupAnnotation) {
//        if (groupAnnotation == null) {
//            return this.readMeta(inputAnnotation);
//        } else {
//            return new Laminate(inputAnnotation, groupAnnotation, meta())
//                    .setValueContext(getContext())
//                    .setDescriptor(getDescriptor());
//        }
//    }

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

//    /**
//     * Create default OuputStream for given Action and given Content
//     *
//     * @param action a {@link hep.dataforge.actions.Action} object.
//     * @param content a {@link hep.dataforge.content.Content} object.
//     * @return a {@link java.io.OutputStream} object.
//     */
//    public OutputStream buildActionOutput(Named content) {
//        return getContext().io().out(getName(), content.getName());
//    }
}
