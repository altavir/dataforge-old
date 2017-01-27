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
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataSet;
import hep.dataforge.description.ActionDescriptor;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.io.reports.Log;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static hep.dataforge.actions.GenericAction.*;

/**
 * A basic implementation of Action interface
 *
 * @param <T>
 * @param <R>
 * @author Alexander Nozik
 */
@ValueDef(name = RESULT_GROUP_KEY, def = "", info = "The name of the data group appended before the path. By default is empty.")
@ValueDef(name = RESULT_NAME_KEY, info = "The override for resulting data name. If not presented, then input data name is used.")
@ValueDef(name = ALLOW_PARALLEL_KEY, type = "BOOLEAN", def = "true", info = "A flag to allow or forbid parallel execution of this action")
public abstract class GenericAction<T, R> implements Action<T, R>, Cloneable {
    public static final String RESULT_GROUP_KEY = "@resultGroup";
    public static final String RESULT_NAME_KEY = "@resultName";
    public static final String ALLOW_PARALLEL_KEY = "@allowParallel";

    protected boolean isParallelExecutionAllowed(Meta meta) {
        return meta.getBoolean("@allowParallel", true);
    }

    /**
     * Generate the name of the resulting data based on name of input data and action meta
     *
     * @param dataName
     * @param actionMeta
     * @return
     */
    protected String getResultName(String dataName, Meta actionMeta) {
        if (actionMeta.hasValue(RESULT_NAME_KEY)) {
            return actionMeta.getString(RESULT_NAME_KEY);
        } else {
            String res = dataName;
            if (actionMeta.hasValue(RESULT_GROUP_KEY)) {
                res = Name.joinString(actionMeta.getString(RESULT_GROUP_KEY), res);
            }
            return res;
        }
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
    protected ExecutorService executor(Context context, Meta meta) {
        if (isParallelExecutionAllowed(meta)) {
            return context.getWorkManager().parallelExecutor();
        } else {
            return context.getWorkManager().singleThreadExecutor();
        }

    }

    /**
     * Return the root process name for this action
     *
     * @return
     */
    protected String getTaskName(Meta actionMeta) {
        return actionMeta.getString("@action.taskName", "action::" + getName());
    }

    protected Logger getLogger(Meta actionMeta) {
        return LoggerFactory.getLogger(actionMeta.getString("@action.logger", getTaskName(actionMeta)));
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
     * <p>
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

    protected Laminate inputMeta(Context context, Meta... meta) {
        return new Laminate(meta)
                .setValueContext(context)
                .setDescriptor(getDescriptor());
    }

    /**
     * Create default OuputStream for given Action and given name
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.OutputStream} object.
     */
    public OutputStream buildActionOutput(Context context, String name) {
        return context.io().out(getName(), name);
    }

    //TODO move to separate manager
    private transient Map<String, Log> reportCache = new ConcurrentHashMap<>();

    protected Log getReport(Context context, String reportName) {
        return reportCache.computeIfAbsent(reportName, (n) -> {
            Log parent = new Log(n, context);
            return new Log(getName(), parent);
        });
    }

    protected final void report(Context context, String reportName, String entry, Object... params) {
        getReport(context, reportName).report(entry, params);
    }
}
