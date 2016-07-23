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

import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataSet;
import hep.dataforge.description.ActionDescriptor;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hep.dataforge.computation.WorkListener;
import hep.dataforge.computation.WorkListenerDelegate;

/**
 * A basic implementation of Action interface
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 * @param <T>
 * @param <R>
 */
public abstract class GenericAction<T, R> implements Action<T, R> {

    private Logger logger;
    private String parentProcessName;
    private Context context = GlobalContext.instance();

    protected boolean isParallelExecutionAllowed(Meta meta) {
        return meta.getBoolean("allowParallel", true);
    }

    /**
     * Wrap result of single or separate executions into DataNode
     *
     * @return
     */
    protected DataNode<R> wrap(String name, Meta meta, Map<String, ? extends Data<R>> result) {
        DataSet.Builder<R> builder = DataSet.builder(getOutputType());
        result.forEach(builder::putData);
        return builder.build();
    }

    protected void checkInput(DataNode input) {
        if (!getInputType().isAssignableFrom(input.type())) {
            //FIXME add specific exception
            throw new RuntimeException(String.format("Type mismatch on action %s start. Expected %s but found %s.",
                    getName(), getInputType().getName(), input.type().getName()));
        }
    }

    /**
     * Get common singleThreadExecutor for this action
     *
     * @return
     */
    protected ExecutorService executor(Meta meta) {
        if(isParallelExecutionAllowed(meta)){
            return getContext().workManager().parallelExecutor();
        } else {
            return getContext().workManager().singleThreadExecutor();
        }
        
    }

    /**
     * Return the root process name for this action
     *
     * @return
     */
    protected String getWorkName() {
        if (parentProcessName != null) {
            return Name.joinString(parentProcessName, getName());
        } else {
            return getName();
        }
    }

    public Logger logger() {
        if (logger == null) {
            return LoggerFactory.getLogger("action::" + getName());
        } else {
            return logger;
        }
    }

    @Override
    public Action<T, R> withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public Action<T, R> withParentProcess(String parentProcessName) {
        this.parentProcessName = parentProcessName;
        return this;
    }

    @Override
    public Action<T, R> withContext(Context context) {
        this.context = context;
        return this;
    }

    @Override
    public Context getContext() {
        return context;
    }

    protected WorkListener workListener(){
        //PENDING make it variable to avoid multiple initialization
        return new WorkListenerDelegate(getContext().workManager(), getWorkName());
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
            throw new RuntimeException("Name not defined");
        }
    }

    /**
     * Input type bases on ActionDef
     *
     * @return
     */
    public Class getInputType() {
        TypedActionDef def = getDef();
        if (getDef() != null) {
            return def.inputType();
        } else {
            return Object.class;
        }
    }

    /**
     * OutputType based on ActionDef
     *
     * @return
     */
    public Class getOutputType() {
        TypedActionDef def = getDef();
        if (getDef() != null) {
            return def.outputType();
        } else {
            return Object.class;
        }
    }

    /**
     * Generate input meta for given data input, group meta and action meta
     *
     * @param input
     * @param nodeMeta
     * @param actionMeta
     * @return
     */
    protected Laminate inputMeta(Data<? extends T> input, Meta nodeMeta, Meta actionMeta) {
        return new Laminate(input.meta(), nodeMeta, actionMeta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    protected Laminate inputMeta(Meta... meta) {
        return new Laminate(meta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    protected Laminate inputMeta(Meta nodeMeta, Meta actionMeta) {
        return new Laminate(nodeMeta, actionMeta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    protected Laminate inputMeta(Meta actionMeta) {
        return new Laminate(actionMeta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    /**
     * Create default OuputStream for given Action and given name
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.OutputStream} object.
     */
    public OutputStream buildActionOutput(String name) {
        return context.io().out(getName(), name);
    }
}
