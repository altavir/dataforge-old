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
import hep.dataforge.names.Named;
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
public abstract class GenericAction<T, R> implements Action<T, R> {

    private Executor executor;

    public Logger logger() {
        //TODO provide from context
        return LoggerFactory.getLogger(getClass());
    }

    protected boolean isParallelExecutionAllowed(Meta meta) {
        return meta.getBoolean("allowParallel", true);
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

    protected Log buildLog(Context context, Meta meta, Object data) {
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
                return new Log(logName, context);
            }
        } else {
            return new Log(logName, context);
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
    protected Laminate inputMeta(Context context, Data<? extends T> input, Meta nodeMeta, Meta actionMeta) {
        return new Laminate(input.meta(), nodeMeta, actionMeta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    protected Laminate inputMeta(Context context, Meta nodeMeta, Meta actionMeta) {
        return new Laminate(nodeMeta, actionMeta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    protected Laminate inputMeta(Context context, Meta actionMeta) {
        return new Laminate(actionMeta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    /**
     * Create default OuputStream for given Action and given name
     *
     * @param action a {@link hep.dataforge.actions.Action} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.OutputStream} object.
     */
    public OutputStream buildActionOutput(Context context, String name) {
        return context.io().out(getName(), name);
    }
}
