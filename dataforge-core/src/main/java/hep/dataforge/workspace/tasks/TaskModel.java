/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace.tasks;

import hep.dataforge.cache.Identifiable;
import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.data.NamedData;
import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Named;
import hep.dataforge.utils.GenericBuilder;
import hep.dataforge.utils.NamingUtils;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import hep.dataforge.workspace.Workspace;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * The model for task execution. Is computed without actual task invocation.
 *
 * @author Alexander Nozik
 */
public class TaskModel implements Named, Metoid, ValueProvider, Identifiable, Encapsulated {

    /**
     * Create an empty model builder
     *
     * @param workspace
     * @param taskName
     * @param taskMeta
     * @return
     */
    public static TaskModel.Builder builder(Workspace workspace, String taskName, @NotNull Meta taskMeta) {
        return new TaskModel.Builder(new TaskModel(workspace, taskName, taskMeta));
    }

    /**
     * The workspace this model belongs to
     */
    private final Workspace workspace;

    /**
     * The unique name of the task
     */
    private String taskName;

    /**
     * Meta for this specific task
     */
    private Meta taskMeta;

    /**
     * A set of dependencies
     */
    private final Set<Dependency> deps;

    /**
     * Copy constructor
     *
     * @param workspace
     * @param taskName
     * @param taskMeta
     * @param deps
     */
    protected TaskModel(Workspace workspace, String taskName, Meta taskMeta, Set<Dependency> deps) {
        this.workspace = workspace;
        this.taskName = taskName;
        this.taskMeta = taskMeta;
        this.deps = deps;
    }

    /**
     * A constructor without dependencies
     *
     * @param workspace
     * @param taskName
     * @param taskMeta
     */
    public TaskModel(Workspace workspace, String taskName, Meta taskMeta) {
        this.workspace = workspace;
        this.taskName = taskName;
        this.taskMeta = taskMeta;
        deps = new LinkedHashSet<>();
    }

    /**
     * Create a copy of this model an delegate it to builder
     *
     * @return
     */
    public Builder builder() {
        return new Builder(this);
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
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

    @Override
    public String getName() {
        return taskName;
    }

    @Override
    public Meta meta() {
        return taskMeta;
    }

    @Override
    public Meta getIdentity() {
        MetaBuilder id = new MetaBuilder("task")
                .setNode(getContext().getIdentity())
                .setValue("name", getName())
                .setNode("meta", meta());

        MetaBuilder depNode = new MetaBuilder("dependencies");

        dependencies().forEach(dependency -> depNode.putNode(dependency.getIdentity()));
        id.putNode(depNode);

        return id;
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
    public interface Dependency extends Identifiable {

        /**
         * Apply data to data dree. Could throw exceptions caused by either
         * calculation or placement procedures.
         *
         * @param tree
         * @param workspace
         */
        void apply(DataTree.Builder<Object> tree, Workspace workspace);
    }

    /**
     * Data dependency
     */
    static class DataDependency implements Dependency {

        /**
         * The gathering function for data
         */
        private final Function<Workspace, Stream<NamedData<?>>> gatherer;
        private final transient Meta id;

        /**
         * The rule to andThen from workspace data name to DataTree path
         */
        private final UnaryOperator<String> pathTransformationRule;

//        public DataDependency(Function<Workspace, Stream<NamedData<?>>> gatherer, UnaryOperator<String> rule) {
//            this.gatherer = gatherer;
//            this.pathTransformationRule = rule;
//        }

        public DataDependency(String mask, UnaryOperator<String> rule) {
            this.gatherer = (w) -> w.getData().dataStream().filter(data -> NamingUtils.wildcardMatch(mask, data.getName()));
            this.pathTransformationRule = rule;
            id = new MetaBuilder("data").putValue("mask", mask);
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
            id = new MetaBuilder("data").putValue("mask", mask).putValue("type", type.getName());
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

        @Override
        public Meta getIdentity() {
            return id;
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

        @Override
        public Meta getIdentity() {
            return new MetaBuilder("dataNode")
                    .putValue("source", sourceNodeName)
                    .putValue("target", targetNodeName)
                    .putValue("type", type.getName());
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

        @Override
        public Meta getIdentity() {
            return taskModel.getIdentity();
        }
    }

    /**
     * A builder for immutable model
     */
    public static class Builder implements GenericBuilder<TaskModel, Builder> {
        private final TaskModel model;
        private MetaBuilder taskMeta = new MetaBuilder();


        public Builder(Workspace workspace, String taskName, @NotNull Meta taskMeta) {
            this.model = new TaskModel(workspace, taskName, Meta.empty());
            this.taskMeta = taskMeta.getBuilder();
        }

        public Builder(Workspace workspace, String taskName) {
            this.model = new TaskModel(workspace, taskName, Meta.empty());
        }

        public Builder(TaskModel model) {
            this.model = model.copy();
            this.taskMeta = model.meta().getBuilder();
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        public TaskModel build() {
            model.taskMeta = taskMeta.build();
            return model;
        }

        public Workspace getWorkspace() {
            return model.getWorkspace();
        }

        public String getName() {
            return this.model.getName();
        }

        public Meta getMeta() {
            return this.model.getMeta();
        }

        /**
         * Apply meta transformation to model meta
         *
         * @param transform
         * @return
         */
        public Builder configure(@NotNull Consumer<MetaBuilder> transform) {
            transform.accept(taskMeta);
            return self();
        }

        /**
         * replace model meta
         *
         * @param meta
         * @return
         */
        public Builder configure(@NotNull Meta meta) {
            this.taskMeta = meta.getBuilder();
            return self();
        }

        /**
         * Rename model
         *
         * @param name
         * @return
         */
        public Builder rename(@NotNull String name) {
            if (name.isEmpty()) {
                throw new AnonymousNotAlowedException();
            } else {
                model.taskName = name;
                return self();
            }
        }

        /**
         * Add dependency on Model with given task
         *
         * @param dep
         * @param as
         */
        public Builder dependsOn(TaskModel dep, String as) {
            model.deps.add(new TaskDependency(dep, as));
            return self();
        }

        /**
         * dependsOn(model, model.getName());
         *
         * @param dep
         */
        public Builder dependsOn(TaskModel dep) {
            return dependsOn(dep, dep.getName());
        }

        /**
         * dependsOn(new TaskModel(workspace, taskName, taskMeta))
         *
         * @param taskName
         * @param taskMeta
         */
        public Builder dependsOn(String taskName, Meta taskMeta) {
            return dependsOn(taskName, taskMeta, "");
        }

        /**
         * dependsOn(new TaskModel(taskName, taskMeta), as);
         *
         * @param taskName
         * @param taskMeta
         * @param as
         */
        public Builder dependsOn(String taskName, Meta taskMeta, String as) {
            return dependsOn(model.workspace.getTask(taskName).build(model.workspace, taskMeta), as);
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
        public Builder data(String mask, UnaryOperator<String> rule) {
            model.deps.add(new DataDependency(mask, rule));
            return self();
        }

        /**
         * Type checked data dependency
         *
         * @param type
         * @param mask
         * @param rule
         * @return
         */
        public Builder data(Class<?> type, String mask, UnaryOperator<String> rule) {
            model.deps.add(new DataDependency(type, mask, rule));
            return self();
        }

        /**
         * data(mask, UnaryOperator.identity());
         *
         * @param mask
         */
        public Builder data(String mask) {
            return data(mask, UnaryOperator.identity());
        }

        /**
         * data(mask, {@code str -> as});
         *
         * @param mask
         * @param as
         */
        public Builder data(String mask, String as) {
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
        public Builder dataNode(Class<?> type, String sourceNodeName, String targetNodeName) {
            model.deps.add(new DataNodeDependency(type, sourceNodeName, targetNodeName));
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
        public Builder dataNode(Class<?> type, String nodeName) {
            model.deps.add(new DataNodeDependency(type, nodeName, nodeName));
            return this;
        }
    }
}
