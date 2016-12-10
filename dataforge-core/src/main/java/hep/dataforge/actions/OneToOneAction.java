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

import hep.dataforge.computation.PipeGoal;
import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.NamedData;
import hep.dataforge.io.reports.Log;
import hep.dataforge.io.reports.Logable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;

import java.util.stream.Collectors;

/**
 * A template to build actions that reflect strictly one to one content
 * transformations
 *
 * @param <T>
 * @param <R>
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class OneToOneAction<T, R> extends GenericAction<T, R> {
    /**
     * Build asynchronous result for single data. Data types separated from
     * action generics to be able to operate maps instead of raw data
     *
     * @param data
     * @param actionMeta
     * @return
     */
    protected ActionResult<R> runOne(Context context, NamedData<? extends T> data, Meta actionMeta) {
        if (!this.getInputType().isAssignableFrom(data.type())) {
            throw new RuntimeException(String.format("Type mismatch in action %s. %s expected, but %s recieved",
                    getName(), getInputType().getName(), data.type().getName()));
        }
        //FIXME add report manager instead of transmitting report
        String resultName = getResultName(data.getName(), actionMeta);
        buildReport(context, resultName, data);
        Laminate meta = inputMeta(context, data.meta(), actionMeta);
        PipeGoal<? extends T, R> goal = new PipeGoal<>(data.getGoal(), executor(context, meta),
                input -> {
                    Thread.currentThread().setName(Name.joinString(getTaskName(actionMeta), resultName));
                    return transform(context, resultName, input, meta);
                }
        );
//        //PENDING a bit ugly solution
//        goal.onStart(() -> workListener(actionMeta).submit(resultName, goal.result()));

        return new ActionResult<>(getReport(context, resultName), goal, outputMeta(data, meta), getOutputType());
    }

    protected void buildReport(Context context, String name, Data<? extends T> data) {
        Logable parent;
        if (data != null && data instanceof ActionResult) {
            Log actionLog = ((ActionResult) data).log();
            if (actionLog.getParent() != null) {
                //Getting parent from previous report
                parent = actionLog.getParent();
            } else {
                parent = new Log(name, context);
            }
        }
    }

    @Override
    public DataNode<R> run(Context context, DataNode<? extends T> set, Meta actionMeta) {
        checkInput(set);
        if (set.isEmpty()) {
            throw new RuntimeException("Running 1 to 1 action on empty data node");
        }

        return wrap(set.getName(), set.meta(),
                set.dataStream(true).collect(Collectors.toMap(data -> getResultName(data.getName(), actionMeta),
                        data -> runOne(context, data, actionMeta))));
    }

    /**
     * @param name      name of the input item
     * @param input     input data
     * @param inputMeta combined meta for this evaluation. Includes data meta,
     *                  group meta and action meta
     * @return
     */
    private R transform(Context context, String name, T input, Laminate inputMeta) {
        beforeAction(context, name, input, inputMeta);
        R res = execute(context, name, input, inputMeta);
        afterAction(context, name, res, inputMeta);
        return res;
    }

    /**
     * Utility method to run action outside of context or execution chain
     *
     * @param input
     * @param metaLayers
     * @return
     */
    public R simpleRun(T input, Meta... metaLayers) {
        Context context = Global.instance();
        return transform(context, "simpleRun", input, inputMeta(context, metaLayers));
    }

    protected abstract R execute(Context context, String name, T input, Laminate inputMeta);

    /**
     * Build output meta for given data. This meta is calculated on action call
     * (no lazy calculations). By default output meta is the same as input data
     * meta.
     *
     * @param inputMeta
     * @param data
     * @return
     */
    protected Meta outputMeta(NamedData<? extends T> data, Meta inputMeta) {
        return data.meta();
    }

    protected void afterAction(Context context, String name, R res, Laminate meta) {
        getLogger(meta).info("Action '{}[{}]' is finished", getName(), name);
    }

    protected void beforeAction(Context context, String name, T datum, Laminate meta) {
        if (context.getBoolean("actions.reportStart", true)) {
            report(context, name, "Starting action {} on data with name {} with following configuration: \n\t {}", getName(), name, meta.toString());
        }
        getLogger(meta).info("Starting action '{}[{}]'", getName(), name);
    }

}
