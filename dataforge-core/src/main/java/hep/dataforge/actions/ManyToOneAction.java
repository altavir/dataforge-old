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
import hep.dataforge.data.DataFactory;
import hep.dataforge.data.DataNode;
import hep.dataforge.io.log.Log;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 * Action with multiple input data pieces but single output
 *
 * @author Alexander Nozik
 * @param <T>
 * @param <R>
 */
public abstract class ManyToOneAction<T, R> extends GenericAction<T, R> {

    @Override
    public DataNode<R> run(Context context, DataNode<T> set, Meta actionMeta) {
        List<DataNode<T>> groups = buildGroups(context, set, actionMeta);
        Map<String, ActionResult<R>> results = new HashMap<>();
        groups.forEach((group) -> results.put(group.getName(), runGroup(context, group, actionMeta)));
        return wrap(set.getName(), set.meta(), results);
    }

    public ActionResult<R> runGroup(Context context, DataNode<T> data, Meta actionMeta) {
        Log log = buildLog(context, data.meta(), data);
        Laminate meta = inputMeta(context, data.meta(), actionMeta);
        Meta outputMeta = outputMeta(data).build();

        //Creating dependency on data
        CompletableFuture<R> future = data.computation()
                .thenCompose((Void t) -> CompletableFuture.supplyAsync(() -> {
                    beforeGroup(context, log, data);
                    // In this moment, all the data is already calculated
                    Map<String, T> collection = data.stream()
                            .collect(Collectors.toMap(item -> item.getKey(), item -> item.getValue().get()));
                    //.<T>map(item -> item.getValue().get()).collect(Collectors.toList());
                    R res = execute(context, log, data.getName(), collection, meta);
                    afterGroup(context, log, data.getName(), outputMeta, res);
                    return res;
                }, buildExecutor(context, data.getName())));
        return new ActionResult<>(getOutputType(), log, future, outputMeta);

    }

    protected List<DataNode<T>> buildGroups(Context context, DataNode<T> input, Meta actionMeta) {
        return GroupBuilder.byAnnotation(inputMeta(context, input.meta(), actionMeta)).group(input);
    }

    protected abstract R execute(Context context, Logable log, String nodeName, Map<String, T> input, Meta meta);

    protected MetaBuilder outputMeta(DataNode<T> input) {
        MetaBuilder builder = new MetaBuilder("node")
                .putValue("name", input.getName())
                .putValue("type", input.type().getName());
        input.stream().forEach((Pair<String, Data<? extends T>> item) -> {
            MetaBuilder dataNode = new MetaBuilder("data")
                    .putValue("name", item.getKey());
            Data<? extends T> data = item.getValue();
            if (!data.dataType().equals(input.type())) {
                dataNode.putValue("type", data.dataType().getName());
            }
            if (!data.meta().isEmpty()) {
                dataNode.putNode(DataFactory.DATA_META_KEY, data.meta());
            }
            builder.putNode(dataNode);
        });
        return builder;
    }

    /**
     * An action to be performed before each group evaluation
     *
     * @param log
     * @param input
     */
    protected void beforeGroup(Context context, Logable log, DataNode<? extends T> input) {

    }

    /**
     * An action to be performed after each group evaluation
     *
     * @param log
     * @param output
     */
    protected void afterGroup(Context context, Logable log, String groupName, Meta outputMeta, R output) {

    }

}
