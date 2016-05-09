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
    protected ActionResult<R> runOne(Context context, String name, Data<? extends T> data, Meta groupMeta, Meta actionMeta) {
        if (!this.getInputType().isAssignableFrom(data.dataType())) {
            throw new RuntimeException(String.format("Type mismatch in action %s. %s expected, but %s recieved",
                    getName(), getInputType().getName(), data.dataType().getName()));
        }

        Report log = buildLog(context, groupMeta, data);
        Laminate meta = inputMeta(context, data, groupMeta, actionMeta);
        //FIXME add error evaluation
        CompletableFuture<R> future = data.getInFuture().
                thenCompose((T datum) -> {
                    return postProcess(context, name, () -> transform(context, log, name, meta, datum));
                });

        return new ActionResult(getOutputType(), log, future, outputMeta(name, groupMeta, data));
    }

//    public ActionResult<R> runOne(Meta meta, NamedData<T> data) {
//        return runOne(data.getName(), data, meta);
//    }
    @Override
    public DataNode<R> run(Context context, DataNode<T> set, Meta actionMeta) {
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
                        entry -> runOne(context, entry.getKey(), entry.getValue(), set.meta(), actionMeta))));
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
    private R transform(Context context, Reportable log, String name, Laminate inputMeta, T input) {
        beforeAction(context, name, input, inputMeta, log);
        R res = execute(context, log, name, inputMeta, input);
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
    public R eval(T input, Meta... metaLayers) {
        Context context = GlobalContext.instance();
        return transform(context, new Report("eval", context), "eval", inputMeta(context, metaLayers), input);
    }

    protected abstract R execute(Context context, Reportable log, String name, Laminate inputMeta, T input);

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

    protected void afterAction(Context context, String name, R res, Laminate meta) {
        logger().info("Action '{}[{}]' is finished", getName(), name);
    }

    protected void beforeAction(Context context, String name, T datum, Laminate meta, Reportable log) {
        logger().info("Starting action '{}[{}]'", getName(), name);
    }

}
