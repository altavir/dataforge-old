/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.cache.Identifiable;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.data.NamedData;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Named;
import hep.dataforge.utils.NamingUtils;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * The model for task execution. Is computed without actual task invocation.
 *
 * @author Alexander Nozik
 */
public class TaskModel implements Named, Metoid, ValueProvider, Identifiable {

    //TODO implement builder chain
    private final Workspace workspace;
    private final String taskName;
    private final Meta taskMeta;
    private final Set<Dependency> deps;
//    private final Set<OutputHook> outs;

    protected TaskModel(Workspace workspace, String taskName, Meta taskMeta, Set<Dependency> deps) {
        this.workspace = workspace;
        this.taskName = taskName;
        this.taskMeta = taskMeta;
        this.deps = deps;
//        this.outs = outs;
    }

    public TaskModel(Workspace workspace, String taskName, Meta taskMeta) {
        this.workspace = workspace;
        this.taskName = taskName;
        this.taskMeta = taskMeta;
        deps = new LinkedHashSet<>();
//        outs = new LinkedHashSet<>();
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public Context getContext() {
        return getWorkspace().getContext();
    }

    /**
     * Shallow copy
     *
     * @return
     */
    public TaskModel copy() {
        return new TaskModel(workspace, taskName, taskMeta, deps);
    }

    /**
     * An ordered collection of dependencies
     *
     * @return
     */
    public Collection<Dependency> dependencies() {
        return deps;
    }
//
//    /**
//     * An ordered collection of task outputs
//     *
//     * @return
//     */
//    public Collection<OutputHook> outs() {
//        return outs;
//    }

//    /**
//     * handle result using
//     *
//     * @param hook
//     */
//    public TaskModel handle(OutputHook hook) {
//        this.outs.add(hook);
//        return this;
//    }

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
    public TaskModel dependsOn(TaskModel model, String as) {
        this.deps.add(new TaskDependency(model, as));
        return this;
    }

    /**
     * dependsOn(model, model.getName());
     *
     * @param model
     */
    public TaskModel dependsOn(TaskModel model) {
        return dependsOn(model, model.getName());
    }

    /**
     * dependsOn(new TaskModel(workspace, taskName, taskMeta))
     *
     * @param taskName
     * @param taskMeta
     */
    public TaskModel dependsOn(String taskName, Meta taskMeta) {
        return dependsOn(taskName, taskMeta, "");
    }

    /**
     * dependsOn(new TaskModel(taskName, taskMeta), as);
     *
     * @param taskName
     * @param taskMeta
     * @param as
     */
    public TaskModel dependsOn(String taskName, Meta taskMeta, String as) {
        return dependsOn(workspace.getTask(taskName).build(workspace, taskMeta), as);
    }

    /**
     * Add data dependency rule using data path mask and name transformation
     * rule.
     * <p>
     * Name change rule should be "pure" to avoid runtime model changes
     *
     * @param mask
     * @param rule
     */
    public TaskModel data(String mask, UnaryOperator<String> rule) {
        this.deps.add(new DataDependency(mask, rule));
        return this;
    }

    /**
     * Type checked data dependency
     *
     * @param type
     * @param mask
     * @param rule
     * @return
     */
    public TaskModel data(Class<?> type, String mask, UnaryOperator<String> rule) {
        this.deps.add(new DataDependency(type, mask, rule));
        return this;
    }

    /**
     * data(mask, UnaryOperator.identity());
     *
     * @param mask
     */
    public TaskModel data(String mask) {
        return data(mask, UnaryOperator.identity());
    }

    /**
     * data(mask, {@code str -> as});
     *
     * @param mask
     * @param as
     */
    public TaskModel data(String mask, String as) {
        //FIXME make smart name transformation here
        return data(mask, str -> as);
    }

    /**
     * Add a dependency on a type checked node
     *
     * @param type
     * @param sourceNodeName
     * @param targetNodeName
     * @return
     */
    @SuppressWarnings("unchecked")
    public TaskModel dataNode(Class<?> type, String sourceNodeName, String targetNodeName) {
        this.deps.add(new DataNodeDependency(type, sourceNodeName, targetNodeName));
        return this;
    }

    /**
     * Source and target node have the same name
     *
     * @param type
     * @param nodeName
     * @return
     */
    @SuppressWarnings("unchecked")
    public TaskModel dataNode(Class<?> type, String nodeName) {
        this.deps.add(new DataNodeDependency(type, nodeName, nodeName));
        return this;
    }

    @Override
    public Meta getIdentity() {
        return new MetaBuilder("task")
                .setNode(getContext().getIdentity())
                .setValue("name", getName())
                .setNode("meta", meta());
    }

    /**
     * Convenience method. Equals {@code meta().getValue(path)}
     *
     * @param path
     * @return
     */
    @Override
    public Optional<Value> optValue(String path) {
        return meta().optValue(path);
    }

    /**
     * Convenience method. Equals {@code meta().hasValue(path)}
     *
     * @param path
     * @return
     */
    @Override
    public boolean hasValue(String path) {
        return meta().hasValue(path);
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
        void apply(DataTree.Builder<Object> tree, Workspace workspace);
    }

//    /**
//     * Task output handler
//     */
//    public interface OutputHook<T> extends BiConsumer<TaskModel, DataNode<T>>, Encapsulated {
//        default Executor getExecutor() {
//            return getContext().singleThreadExecutor();
//        }
//
//        default Consumer<DataNode<T>> handler(TaskModel model) {
//            return node -> this.accept(model, node);
//        }
//    }

    /**
     * Data dependency
     */
    static class DataDependency implements Dependency{

        /**
         * The gathering function for data
         */
        private final Function<Workspace, Stream<NamedData<?>>> gatherer;

        /**
         * The rule to andThen from workspace data name to DataTree path
         */
        private final UnaryOperator<String> pathTransformationRule;

        public DataDependency(Function<Workspace, Stream<NamedData<?>>> gatherer, UnaryOperator<String> rule) {
            this.gatherer = gatherer;
            this.pathTransformationRule = rule;
        }

        public DataDependency(String mask, UnaryOperator<String> rule) {
            this.gatherer = (w) -> w.getData().dataStream().filter(data -> NamingUtils.wildcardMatch(mask, data.getName()));
            this.pathTransformationRule = rule;
        }

        /**
         * Data dependency w
         *
         * @param type
         * @param mask
         * @param rule
         */
        public DataDependency(Class<?> type, String mask, UnaryOperator<String> rule) {
            this.gatherer = (w) -> w.getData().dataStream().filter(data -> NamingUtils.wildcardMatch(mask, data.getName())
                    && type.isAssignableFrom(data.type()));
            this.pathTransformationRule = rule;
        }

        /**
         * Place data
         *
         * @param tree
         * @param workspace
         */
        @Override
        public void apply(DataTree.Builder<Object> tree, Workspace workspace) {
            gatherer.apply(workspace)
                    .forEach(data -> tree.putData(pathTransformationRule.apply(data.getName()), data));
        }
    }

    static class DataNodeDependency<T> implements Dependency {
        private final String sourceNodeName;
        private final String targetNodeName;
        private final Class<T> type;

        public DataNodeDependency(Class<T> type, String sourceNodeName, String targetNodeName) {
            this.sourceNodeName = sourceNodeName;
            this.targetNodeName = targetNodeName;
            this.type = type;
        }

        @Override
        public void apply(DataTree.Builder<Object> tree, Workspace workspace) {
            tree.putNode(targetNodeName, workspace.getData().getCheckedNode(sourceNodeName, type));
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
        BiConsumer<DataTree.Builder<Object>, DataNode<?>> placementRule;

        public TaskDependency(TaskModel taskModel, BiConsumer<DataTree.Builder<Object>, DataNode<?>> rule) {
            this.taskModel = taskModel;
            this.placementRule = rule;
        }

        public TaskDependency(TaskModel taskModel, String as) {
            this.taskModel = taskModel;
            if (as.isEmpty()) {
                this.placementRule = (DataTree.Builder<Object> tree, DataNode<?> result) -> {
                    if (!result.meta().isEmpty()) {
                        if (tree.meta().isEmpty()) {
                            tree.setMeta(result.meta());
                        } else {
                            LoggerFactory.getLogger(getClass()).error("Root node meta already exists.");
                        }
                    }
                    result.dataStream().forEach(tree::putData);
                };
            } else {
                this.placementRule = (DataTree.Builder<Object> tree, DataNode<?> result) -> tree.putNode(as, result);
            }
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
        public void apply(DataTree.Builder<Object> tree, Workspace workspace) {
            placementRule.accept(tree, workspace.runTask(taskModel));
        }
    }
}
