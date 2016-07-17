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

import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.io.reports.Report;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * A template to build actions that reflect strictly one to one content
 * transformations
 *
 * @author Alexander Nozik
 * @param <T>
 * @param <R>
 * @version $Id: $Id
 */
public abstract class OneToOneAction<T, R> extends GenericAction<T, R> {

    /**
     * Build asynchronous result for single data. Data types separated from
     * action generics to be able to operate maps instead of raw data
     *
     * @param context
     * @param name
     * @param groupMeta
     * @param data
     * @param actionMeta
     * @return
     */
    protected ActionResult<R> runOne(String name, Data<? extends T> data, Meta groupMeta, Meta actionMeta) {
        if (!this.getInputType().isAssignableFrom(data.dataType())) {
            throw new RuntimeException(String.format("Type mismatch in action %s. %s expected, but %s recieved",
                    getName(), getInputType().getName(), data.dataType().getName()));
        }

        Report report = buildReport(name, data);
        Laminate meta = inputMeta(data, groupMeta, actionMeta);
        //FIXME add error evaluation

        CompletableFuture<R> future = data.getInFuture().
                thenCompose((T datum) -> {
                    return postProcess(name, () -> transform(report, name, meta, datum));
                });

        return new ActionResult(getOutputType(), report, future, outputMeta(name, groupMeta, data));
    }

    protected Report buildReport(String name, Data<? extends T> data) {
        Reportable parent;
        if (data != null && data instanceof ActionResult) {
            Report actionLog = ((ActionResult) data).log();
            if (actionLog.getParent() != null) {
                //Getting parent from previous report
                parent = actionLog.getParent();
            } else {
                parent = new Report(name, getContext());
            }
        } else {
            parent = new Report(name, getContext());
        }
        return new Report(getName(), parent);
    }

    @Override
    public DataNode<R> run(DataNode<T> set, Meta actionMeta) {
        checkInput(set);
        if (set.isEmpty()) {
            throw new RuntimeException("Running 1 to 1 action on empty data node");
        }

        Stream<Pair<String, Data<? extends T>>> stream = set.dataStream();
        if (isParallelExecutionAllowed(actionMeta)) {
            stream = stream.parallel();
        }

        return wrap(set.getName(), set.meta(),
                stream.collect(Collectors.toMap(entry -> entry.getKey(),
                        entry -> runOne(entry.getKey(), entry.getValue(), set.meta(), actionMeta))));
    }

    /**
     *
     * @param log report for this evaluation
     * @param name name of the input item
     * @param inputMeta combined meta for this evaluation. Includes data meta,
     * group meta and action meta
     * @param input input data
     * @return
     */
    private R transform(Reportable log, String name, Laminate inputMeta, T input) {
        beforeAction(name, input, inputMeta, log);
        R res = execute(log, name, inputMeta, input);
        afterAction(name, res, inputMeta);
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
        return transform(new Report("simpleRun", getContext()), "simpleRun", inputMeta(metaLayers), input);
    }

    protected abstract R execute(Reportable log, String name, Laminate inputMeta, T input);

    /**
     * Build output meta for given data. This meta is calculated on action call
     * (no lazy calculations). By default output meta is the same as input data
     * meta.
     *
     * @param name
     * @param inputMeta
     * @param data
     * @return
     */
    protected Meta outputMeta(String name, Meta inputMeta, Data<? extends T> data) {
        return data.meta();
    }

    protected void afterAction(String name, R res, Laminate meta) {
        logger().info("Action '{}[{}]' is finished", getName(), name);
    }

    protected void beforeAction(String name, T datum, Laminate meta, Reportable report) {
        if (getContext().getBoolean("actions.reportStart", true)) {
            report.report("Starting action {} on data with name {} with following configuration: \n\t {}", getName(), name, meta.toString());
        }
        logger().info("Starting action '{}[{}]'", getName(), name);
    }

}
