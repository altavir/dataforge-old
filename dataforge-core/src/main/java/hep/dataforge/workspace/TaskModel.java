/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.computation.WorkManager;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.utils.NamingUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * The model for task execution. Is computed without actual task invocation.
 *
 * @author Alexander Nozik
 */
public class TaskModel implements Named, Annotated {

    private final Workspace workspace;
    private final String taskName;
    private final Meta taskMeta;
    private final Set<Dependency> deps;
    private final Set<TaskOutput> outs;

    protected TaskModel(Workspace workspace, String taskName, Meta taskMeta, Set<Dependency> deps, Set<TaskOutput> outs) {
        this.workspace = workspace;
        this.taskName = taskName;
        this.taskMeta = taskMeta;
        this.deps = deps;
        this.outs = outs;
    }

    public TaskModel(Workspace workspace, String taskName, Meta taskMeta) {
        this.workspace = workspace;
        this.taskName = taskName;
        this.taskMeta = taskMeta;
        deps = new LinkedHashSet<>();
        outs = new LinkedHashSet<>();
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Shallow copy
     * @return 
     */
    public TaskModel copy(){
        return new TaskModel(workspace, taskName, taskMeta, deps, outs);
    }
    
    /**
     * An ordered collection of dependencies
     *
     * @return
     */
    public Collection<Dependency> dependencies() {
        return deps;
    }

    /**
     * An ordered collection of task outputs
     *
     * @return
     */
    public Collection<TaskOutput> outs() {
        return outs;
    }

    /**
     * Add output action to task model. Order matters.
     *
     * @param consumer
     */
    public void out(BiConsumer<Context, TaskState> consumer) {
        TaskOutput out = (WorkManager.Callback callback, Context context, TaskState state) -> {
            callback.getManager().post(callback.workName() + ".output", () -> consumer.accept(context, state));
        };
        this.outs.add(out);
    }

    @Override
    public String getName() {
        return taskName;
    }

    @Override
    public Meta meta() {
        return taskMeta;
    }

    /**
     * Add dependency on Model with given task
     *
     * @param model
     * @param as
     */
    public void dependsOn(TaskModel model, String as) {
        this.deps.add(new TaskDependency(model, as));
    }

    /**
     * dependsOn(model, model.getName());
     *
     * @param model
     */
    public void dependsOn(TaskModel model) {
        dependsOn(model, model.getName());
    }

    /**
     * dependsOn(new TaskModel(taskName, taskMeta), as);
     *
     * @param taskName
     * @param taskMeta
     * @param as
     */
    public void dependsOn(String taskName, Meta taskMeta, String as) {
        dependsOn(new TaskModel(workspace, taskName, taskMeta), as);
    }

    /**
     * Add data dependency rule using data path mask and name transformation
     * rule.
     *
     * @param mask
     * @param rule
     */
    //FIXME Name change rule should be "pure" to avoid runtime model changes
    public void data(String mask, UnaryOperator<String> rule) {
        this.deps.add(new DataDependency(mask, rule));
    }

    /**
     * data(mask, UnaryOperator.identity());
     *
     * @param mask
     */
    public void data(String mask) {
        data(mask, UnaryOperator.identity());
    }

    /**
     * data(mask, str -> as);
     *
     * @param mask
     * @param as
     */
    public void data(String mask, String as) {
        //FIXME make smart name transformation here
        data(mask, str -> as);
    }

    /**
     * A rule to add calculate dependency data from workspace
     */
    public interface Dependency {

        /**
         * Apply data to data dree. Could throw exceptions caused by either
         * calculation or placement procedures.
         *
         * @param tree
         * @param workspace
         */
        public void apply(DataTree.Builder tree, Workspace workspace);
    }

    /**
     * Data dependency
     */
    static class DataDependency implements Dependency {

        /**
         * The gathering function for data
         */
        private final Function<Workspace, Stream<Pair<String, Data<?>>>> gatherer;

        /**
         * The rule to transform from workspace data name to DataTree path
         */
        private final UnaryOperator<String> pathTransformationRule;

        public DataDependency(Function<Workspace, Stream<Pair<String, Data<?>>>> gatherer, UnaryOperator<String> rule) {
            this.gatherer = gatherer;
            this.pathTransformationRule = rule;
        }

        public DataDependency(String mask, UnaryOperator<String> rule) {
            this.gatherer = (w) -> w.getDataStage().dataStream().filter(pair -> NamingUtils.wildcardMatch(mask, pair.getKey()));
            this.pathTransformationRule = rule;
        }

        /**
         * Place data
         *
         * @param tree
         * @param workspace
         */
        @Override
        public void apply(DataTree.Builder tree, Workspace workspace) {
            gatherer.apply(workspace)
                    .forEach(pair -> tree.putData(pathTransformationRule.apply(pair.getKey()), pair.getValue()));
        }
    }

    /**
     * Task dependency
     */
    static class TaskDependency implements Dependency {
        //TODO make meta configurable

        /**
         * The model of task
         */
        TaskModel taskModel;

        /**
         * The rule to attach dependency data to data node when it is calculated
         */
        BiConsumer<DataTree.Builder, DataNode> placementRule;

        public TaskDependency(TaskModel taskModel, BiConsumer<DataTree.Builder, DataNode> rule) {
            this.taskModel = taskModel;
            this.placementRule = rule;
        }

        public TaskDependency(TaskModel taskModel, String as) {
            this.taskModel = taskModel;
            this.placementRule = (DataTree.Builder tree, DataNode result) -> tree.putNode(as, result);
        }

        /**
         * The model of task
         *
         * @return
         */
        public TaskModel model() {
            return taskModel;
        }

        /**
         * Attach result of task execution to the data tree
         *
         * @param tree
         * @param workspace
         */
        @Override
        public void apply(DataTree.Builder tree, Workspace workspace) {
            placementRule.accept(tree, workspace.runTask(taskModel));
        }
    }

    /**
     * Task output
     */
    public interface TaskOutput {

        void accept(WorkManager.Callback callback, Context context, TaskState state);
    }
}
