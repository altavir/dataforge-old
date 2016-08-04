/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.actions.Action;
import hep.dataforge.actions.ActionManager;
import static hep.dataforge.actions.ActionUtils.ACTION_NODE_KEY;
import static hep.dataforge.actions.ActionUtils.ACTION_TYPE;
import static hep.dataforge.actions.ActionUtils.SEQUENCE_ACTION_TYPE;
import static hep.dataforge.actions.ActionUtils.buildAction;

import hep.dataforge.context.Context;
import hep.dataforge.computation.WorkManager;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Template;
import hep.dataforge.utils.GenericBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.util.Pair;
import hep.dataforge.utils.ContextMetaFactory;

/**
 * A builder for custom task. Does not ensure type safety.
 *
 * @author Alexander Nozik
 */
public class TaskBuilder implements GenericBuilder<Task, TaskBuilder> {

    private String name = "@default";
    /**
     * A list of actions inside task
     */
    private final List<Pair<Function<Context, Action>, ContextMetaFactory<Meta>>> actions = new ArrayList<>();
    /**
     * Transformations of TaskModel
     */
    private final List<ModelTransformation> modelTransformations = new ArrayList<>();


    public TaskBuilder setName(String name) {
        this.name = name;
        return self();
    }

    /**
     * Lazy action initialization from context ActionManager
     *
     * @param actionName
     * @param metaBuilder
     * @return
     */
    public TaskBuilder doLast(String actionName, ContextMetaFactory<Meta> metaBuilder) {
        actions.add(new Pair<>(ctx -> ActionManager.buildFrom(ctx).getAction(actionName), metaBuilder));
        return self();
    }

    /**
     * Lazy action initialization from Class default constructor
     *
     * @param actionClass
     * @param metaBuilder
     * @return
     */
    public TaskBuilder doLast(Class<? extends Action> actionClass, ContextMetaFactory<Meta> metaBuilder) {
        actions.add(new Pair<>(ctx -> {
            try {
                return actionClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException("Can't initialize action", ex);
            }
        }, metaBuilder));
        return self();
    }

    /**
     * Add last action with given transformation from task meta to specific
     * action meta
     *
     * @param action
     * @param metaBuilder
     * @return
     */
    public TaskBuilder doLast(Action action, ContextMetaFactory<Meta> metaBuilder) {
        actions.add(new Pair<>(ctx -> action, metaBuilder));
        return self();
    }

    /**
     * Perform an action using given meta as a template and actual task meta as
     * template data
     *
     * @param action
     * @param actionMeta
     * @return
     */
    public TaskBuilder doLast(Action action, Meta actionMeta) {
        return doLast(action, (ctx, meta) -> Template.compileTemplate(actionMeta, meta));
    }

    /**
     * Perform action without meta
     *
     * @param action
     * @return
     */
    public TaskBuilder doLast(Action action) {
        return doLast(action, (ctx, meta) -> Meta.empty());
    }

    /**
     * Add first action
     *
     * @param action
     * @param metaBuilder
     * @return
     */
    public TaskBuilder doFirst(Action action, ContextMetaFactory<Meta> metaBuilder) {
        actions.add(0, new Pair<>(ctx -> action, metaBuilder));
        return self();
    }

    public TaskBuilder doFirst(Action action, Meta actionMeta) {
        return doFirst(action, (ctx, meta) -> Template.compileTemplate(actionMeta, meta));
    }

    /**
     * Apply model transformation during model preparation phase.
     * <strong> WARNING: The order of transformation could affect the result.
     * </strong>
     *
     * @param transformation
     * @return
     */
    public TaskBuilder dependencyRule(ModelTransformation transformation) {
        this.modelTransformations.add(transformation);
        return self();
    }

    /**
     * Add default dependency of task being built on given Task.
     *
     * @param taskName
     * @param as
     * @param metaTransformation a transformation to extract dependency meta
     * @return
     */
    public TaskBuilder dependsOn(String taskName, String as, UnaryOperator<Meta> metaTransformation) {
        return dependencyRule((TaskModel model) -> {
            Workspace workspace = model.getWorkspace();
            Meta depMeta = metaTransformation.apply(model.meta());
            workspace.getTask(taskName).build(workspace, depMeta);
            model.dependsOn(workspace.getTask(taskName).build(workspace, depMeta), as);
        });
    }

    /**
     * Add dependency on specific data. Name patterns are not allowed.
     *
     * @param dataName
     * @param as
     * @return
     */
    public TaskBuilder data(String dataName, String as) {
        return dependencyRule((TaskModel model) -> {
            model.data(dataName, as);
        });
    }

    /**
     * Add dependency on data group using given pattern.
     *
     * @param predicate
     * @return
     */
    public TaskBuilder data(Predicate<Pair<String, Data<?>>> predicate) {
        return dependencyRule((TaskModel model) -> {
            model.getWorkspace().getDataStage().dataStream()
                    .filter(predicate)
                    .forEach(pair -> model.data(pair.getKey()));
        });
    }

    /**
     * Add all data with name matching any of presented patterns. Patterns could contain ? and * wildcards
     * @param namePattern
     * @return 
     */
    public TaskBuilder data(String... namePattern) {
        Predicate<Pair<String, Data<?>>> pattern = pair -> false;
        for (String n : namePattern) {
            pattern = pattern.or(pair -> pair.getKey().matches(n.replace("?", ".?").replace("*", ".*?")));
        }
        return data(pattern);
    }

    /**
     * apply action configuration from meta node
     *
     * @param context - context used for action building
     * @param actionsMeta
     * @return
     */
    public TaskBuilder fromMeta(Context context, Meta actionsMeta) {
        actionsMeta.getNodes(ACTION_NODE_KEY).stream().forEach((action) -> {
            String actionType = action.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
            doLast(buildAction(context, actionType), action);
        });
        return self();
    }

    @Override
    public TaskBuilder self() {
        return this;
    }

    @Override
    public Task build() {
        return new CustomTask();
    }

    public interface ModelTransformation {

        void apply(TaskModel model);
    }

    private class CustomTask extends GenericTask {

        @Override
        protected TaskState transform(WorkManager.Callback callback, Context context, TaskState state, Meta config) {
            DataNode res = state.getData();
            for (Pair<Function<Context, Action>, ContextMetaFactory<Meta>> pair : actions) {
                Action action = pair.getKey().apply(context);
                Meta actionMeta = pair.getValue().build(context, config);
                res = action.withParentProcess(callback.workName()).run(res, actionMeta);
                if (actionMeta.hasValue("stageName")) {
                    state.setData(actionMeta.getString("stageName"), res);
                }
            }
            state.finish(res);
            return state;
        }

        @Override
        protected TaskModel transformModel(TaskModel model) {
            super.transformModel(model);
            modelTransformations.stream().forEach(dep -> {
                dep.apply(model);
            });
            return model;
        }

        @Override
        public String getName() {
            return name;
        }

    }

}
