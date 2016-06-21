/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.actions.Action;
import static hep.dataforge.actions.ActionUtils.ACTION_NODE_KEY;
import static hep.dataforge.actions.ActionUtils.ACTION_TYPE;
import static hep.dataforge.actions.ActionUtils.SEQUENCE_ACTION_TYPE;
import static hep.dataforge.actions.ActionUtils.buildAction;
import hep.dataforge.context.Context;
import hep.dataforge.context.ProcessManager;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Template;
import hep.dataforge.utils.GenericBuilder;
import hep.dataforge.utils.MetaFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import javafx.util.Pair;

/**
 * A builder for custom task. does not ensure type safety.
 *
 * @author Alexander Nozik
 */
public class TaskBuilder implements GenericBuilder<Task, TaskBuilder> {

    private String name;
    /**
     * A list of actions inside task
     */
    private final List<Pair<Action, MetaFactory<Meta>>> actions = new ArrayList<>();
    /**
     * Transformations of TaskModel
     */
    private final List<ModelTransformation> modelTransformations = new ArrayList<>();

    /**
     * Add last action with given transformation from task meta to specific
     * action meta
     *
     * @param action
     * @param metaBuilder
     * @return
     */
    public TaskBuilder doLast(Action action, MetaFactory<Meta> metaBuilder) {
        actions.add(new Pair<>(action, metaBuilder));
        return self();
    }

    public TaskBuilder doLast(Action action, Meta actionMeta) {
        return doLast(action, (ctx, meta) -> Template.compileTemplate(actionMeta, meta));
    }

    /**
     * Add first action
     *
     * @param action
     * @param metaBuilder
     * @return
     */
    public TaskBuilder doFirst(Action action, MetaFactory<Meta> metaBuilder) {
        actions.add(0, new Pair<>(action, metaBuilder));
        return self();
    }

    public TaskBuilder doFirst(Action action, Meta actionMeta) {
        return doFirst(action, (ctx, meta) -> Template.compileTemplate(actionMeta, meta));
    }

    /**
     * Apply model transformation during model preparation phase.
     * <strong> WARNING: The order of transformation could affect the result. </strong>
     *
     * @param transformation
     * @return
     */
    public TaskBuilder transformModel(ModelTransformation transformation) {
        this.modelTransformations.add(transformation);
        return self();
    }
    
    /**
     * Add default dependency of task being built on given Task.
     * @param taskName
     * @param as
     * @param metaTransformation a transformation to extract dependency meta 
     * @return 
     */
    public TaskBuilder dependsOnTask(String taskName, String as, UnaryOperator<Meta> metaTransformation) {
        return transformModel((Workspace workspace, Meta taskMeta, TaskModel model) -> {
            Meta depMeta = metaTransformation.apply(taskMeta);
            workspace.getTask(taskName).model(workspace, depMeta);
            model.dependsOn(workspace.getTask(taskName).model(workspace, depMeta), as);
        });
    }
    
    public TaskBuilder dependsOnData(String dataName, String as){
        return transformModel((Workspace workspace, Meta taskMeta, TaskModel model) -> {
            model.dependsOnData(dataName, as);
        });
    }

    /**
     * apply action configuration from meta node
     *
     * @param context - context used for action building
     * @param actionsMeta
     * @return
     */
    public TaskBuilder applyActionMeta(Context context, Meta actionsMeta) {
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

        void apply(Workspace workspace, Meta taskMeta, TaskModel model);
    }

    private class CustomTask extends GenericTask {

        @Override
        protected TaskState transform(ProcessManager.Callback callback, Context context, TaskState state, Meta config) {
            DataNode res = state.getData();
            for (Pair<Action, MetaFactory<Meta>> pair : actions) {
                Action action = pair.getKey();
                Meta actionMeta = pair.getValue().build(context, config);
                res = action.withParentProcess(callback.processName()).run(context, res, actionMeta);
                if (actionMeta.hasValue("stageName")) {
                    state.setData(actionMeta.getString("stageName"), res);
                }
            }
            state.finish(res);
            return state;
        }

        @Override
        public TaskModel model(Workspace workspace, Meta taskMeta) {
            TaskModel model = super.model(workspace, taskMeta);
            modelTransformations.stream().forEach(dep -> {
                dep.apply(workspace, taskMeta, model);
            });
            return model;
        }

        @Override
        public String getName() {
            return name;
        }

    }

}
