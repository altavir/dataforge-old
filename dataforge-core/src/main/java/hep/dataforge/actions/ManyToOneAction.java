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
import hep.dataforge.data.DataFactory;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.NamedData;
import hep.dataforge.goals.AbstractGoal;
import hep.dataforge.goals.Goal;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Action with multiple input data pieces but single output
 *
 * @param <T>
 * @param <R>
 * @author Alexander Nozik
 */
public abstract class ManyToOneAction<T, R> extends GenericAction<T, R> {


    public ManyToOneAction(String name) {
        super(name);
    }

    public ManyToOneAction() {
    }

    @Override
    public DataNode<R> run(Context context, DataNode<? extends T> set, Meta actionMeta) {
        checkInput(set);
        List<DataNode<T>> groups = buildGroups(context, set, actionMeta);
        Map<String, ActionResult<R>> results = new HashMap<>();

        groups.forEach((group) -> results.put(group.getName(), runGroup(context, group, actionMeta)));
        return wrap(getResultName(set.getName(), actionMeta), set.meta(), results);
    }

    public ActionResult<R> runGroup(Context context, DataNode<T> data, Meta actionMeta) {
        Meta outputMeta = outputMeta(data).build();
        Goal<R> goal = new ManyToOneGoal(context, data, actionMeta, outputMeta);
        String reportName = data.getName();
        if (reportName.isEmpty()) {
            reportName = getName();
        }

        return new ActionResult<>(context.getLog(reportName), goal, outputMeta, getOutputType());
    }

    protected List<DataNode<T>> buildGroups(Context context, DataNode<? extends T> input, Meta actionMeta) {
        //TODO expand grouping options
        if (actionMeta.hasMeta("byValue")) {
            return GroupBuilder.byMeta(inputMeta(context, input.meta(), actionMeta)).group(input);
        } else {
            return Collections.singletonList((DataNode<T>) input);
        }
    }

    /**
     * Perform actual calculation
     *
     * @param nodeName
     * @param input
     * @param meta
     * @return
     */
    protected abstract R execute(Context context, String nodeName, Map<String, T> input, Laminate meta);

    /**
     * Build output meta for resulting object
     *
     * @param input
     * @return
     */
    protected MetaBuilder outputMeta(DataNode<T> input) {
        MetaBuilder builder = new MetaBuilder("node")
                .putValue("name", input.getName())
                .putValue("type", input.type().getName());
        input.dataStream().forEach((NamedData<? extends T> data) -> {
            MetaBuilder dataNode = new MetaBuilder("data")
                    .putValue("name", data.getName());
            if (!data.type().equals(input.type())) {
                dataNode.putValue("type", data.type().getName());
            }
            if (!data.meta().isEmpty()) {
                dataNode.putNode(DataFactory.NODE_META_KEY, data.meta());
            }
            builder.putNode(dataNode);
        });
        return builder;
    }

    /**
     * An action to be performed before each group evaluation
     *
     * @param input
     */
    protected void beforeGroup(Context context, DataNode<? extends T> input) {

    }

    /**
     * An action to be performed after each group evaluation
     *
     * @param output
     */
    protected void afterGroup(Context context, String groupName, Meta outputMeta, R output) {

    }

    /**
     * Action  goal {@code fainOnError()} delegate
     *
     * @return
     */
    protected boolean failOnError() {
        return true;
    }

    private class ManyToOneGoal extends AbstractGoal<R> {

        private final Context context;
        private final DataNode<T> data;
        private final Meta actionMeta;
        private final Meta outputMeta;

        public ManyToOneGoal(Context context, DataNode<T> data, Meta actionMeta, Meta outputMeta) {
            super(executor(context, actionMeta));
            this.context = context;
            this.data = data;
            this.actionMeta = actionMeta;
            this.outputMeta = outputMeta;
        }

        @Override
        protected boolean failOnError() {
            return ManyToOneAction.this.failOnError();
        }

        @Override
        public Stream<Goal> dependencies() {
            return data.nodeGoal().dependencies();
        }

        @Override
        protected R compute() throws Exception {
            Laminate meta = inputMeta(context, data.meta(), actionMeta);
            Thread.currentThread().setName(Name.joinString(getTaskName(actionMeta), data.getName()));
            beforeGroup(context, data);
            // In this moment, all the data is already calculated
            Map<String, T> collection = data.dataStream()
                    .filter(it -> it.isValid()) // filter valid data only
                    .collect(Collectors.toMap(d -> d.getName(), d -> d.get()));
            R res = execute(context, data.getName(), collection, meta);
            afterGroup(context, data.getName(), outputMeta, res);
            return res;
        }

    }

}
