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
import hep.dataforge.io.log.Log;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.util.Pair;

/**
 * Action with multiple input data pieces but single output
 *
 * @author Alexander Nozik
 * @param <T>
 * @param <R>
 */
public abstract class ManyToOneAction<T, R> extends GenericAction<T, R> {

    public ManyToOneAction(Context context, String name, Meta annotation) {
        super(context, name, annotation);
    }

    public ManyToOneAction(Context context, Meta annotation) {
        super(context, annotation);
    }

    @Override
    public DataNode<R> run(DataNode<T> set) {
        List<DataNode<T>> groups = buildGroups(set);
        Map<String, ActionResult<R>> results = new HashMap<>();
        groups.forEach((group) -> results.put(group.getName(), runGroup(group)));
        return wrap(set.getName(), set.meta(), results);
    }

    public ActionResult<R> runGroup(DataNode<T> data) {
        Log log = buildLog(data.meta(), data);
        Meta outputMeta = outputMeta(data).build();
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            beforeGroup(log, data);
            R res = execute(log, data);
            afterGroup(log, data.getName(), outputMeta, res);
            return res;
        }, buildExecutor(data.meta(), data));
        return new ActionResult<>(getOutputType(), log, future, outputMeta);

    }

    protected List<DataNode<T>> buildGroups(DataNode<T> input) {
        return GroupBuilder.byAnnotation(inputMeta(input.meta())).group(input);
    }

    protected abstract R execute(Logable log, DataNode<T> input);

    protected MetaBuilder outputMeta(DataNode<T> input){
        MetaBuilder builder = new MetaBuilder("node")
                .putValue("name", input.getName())
                .putValue("type", input.type().getName());
        input.stream().forEach((Pair<String, Data<? extends T>> item) -> {
            MetaBuilder dataNode = new MetaBuilder("data")
                    .putValue("name", item.getKey());
            Data<? extends T> data = item.getValue();
            if(!data.dataType().equals(input.type())){
                dataNode.putValue("type", data.dataType().getName());
            }
            if(!data.meta().isEmpty()){
                dataNode.putNode("@meta", data.meta());
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
    protected void beforeGroup(Logable log, DataNode<? extends T> input) {

    }

    /**
     * An action to be performed after each group evaluation
     *
     * @param log
     * @param output
     */
    protected void afterGroup(Logable log, String groupName, Meta outputMeta, R output) {

    }

}
