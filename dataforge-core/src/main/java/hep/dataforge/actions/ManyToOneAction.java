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

import hep.dataforge.content.Content;
import hep.dataforge.context.Context;
import hep.dataforge.dependencies.DataNode;
import hep.dataforge.io.log.Log;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Action with multiple input data pieces but single output
 *
 * @author Alexander Nozik
 * @param <T>
 * @param <R>
 */
public abstract class ManyToOneAction<T extends Content, R extends Content> extends GenericAction<T, R> {

    public ManyToOneAction(Context context, String name, Meta annotation) {
        super(context, name, annotation);
    }

    public ManyToOneAction(Context context, Meta annotation) {
        super(context, annotation);
    }

    @Override
    public DataNode<R> run(DataNode<T> set) {
        List<DataNode<? extends T>> groups = buildGroups(set);
        Map<String, ActionResult<R>> results = new HashMap<>();
        groups.forEach((group) -> results.put(group.getName(), runGroup(group)));
        return wrap(set.getName(), set.meta(), results);
    }

    public ActionResult<R> runGroup(DataNode<? extends T> data) {
        Log log = buildLog(data.meta(), data);
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            beforeGroup(log, data);
            R res = execute(log, data);
            afterGroup(log, data.getName(), res);
            return res;
        }, buildExecutor(data.meta(), data));
        return new ActionResult<>(getOutputType(), log, future);

    }

    protected abstract List<DataNode<? extends T>> buildGroups(DataNode<? extends T> input);

    protected abstract R execute(Logable log, DataNode<? extends T> input);

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
    protected void afterGroup(Logable log, String groupName, R output) {

    }

}
