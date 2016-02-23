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
import hep.dataforge.dependencies.Data;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import hep.dataforge.dependencies.DataNode;
import hep.dataforge.dependencies.DataSet;
import hep.dataforge.dependencies.NamedData;
import hep.dataforge.io.log.Log;
import java.util.Map;
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

    public OneToOneAction(Context context, String name, Meta annotation) {
        super(context, name, annotation);
    }

    public OneToOneAction(Context context, Meta annotation) {
        super(context, annotation);
    }

    /**
     * Build asynchronous result for single data. Data types separated from
     * action generics to be able to operate maps instead of raw data
     *
     * @param meta
     * @param data
     * @return
     */
    public ActionResult<R> runOne(String name, Meta meta, Data<? extends T> data) {
        ActionResult<T> previous = (ActionResult<T>) data;
        Log log = buildLog(meta, previous);
        CompletableFuture<R> future = previous.getInFuture().
                thenCompose((T t) -> CompletableFuture
                        .supplyAsync(() -> transform(name, log, meta, t), buildExecutor(meta, data)));

        return new ActionResult(getOutputType(),
                log,
                future);
    }
    
    public ActionResult<R> runOne(Meta meta, NamedData<T> data){
        return runOne(data.getName(), meta, data);
    }    

    @Override
    public DataNode<R> run(DataNode<T> set) {
        Stream<Pair<String, Data<? extends T>>> stream = set.stream();
        if (isParallelExecutionAllowed()) {
            stream = stream.parallel();
        }
        return wrap(set.getName(), set.meta(),
                stream.collect(Collectors.toMap(entry -> entry.getKey(),
                        entry -> runOne(entry.getKey(), set.meta(), entry.getValue()))));
    }
    
    private R transform(String name, Logable log, Meta meta, T input) {
        beforeAction(name, input, log);
        R res = execute(log, meta, input);
        afterAction(name, res);
        return res;
    }

    protected abstract R execute(Logable log, Meta meta, T input);

    protected void afterAction(String name, R res) {
        logger().info("Action '{}[{}]' is finished", getName(), name);
    }

    protected void beforeAction(String name, T datum, Logable log) {
        logger().info("Starting action '{}[{}]'", getName(), name);
    }

}
