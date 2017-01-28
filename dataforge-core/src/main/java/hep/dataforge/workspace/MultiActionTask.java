package hep.dataforge.workspace;

import hep.dataforge.actions.Action;
import hep.dataforge.actions.ActionUtils;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A task defined as a composition of multiple actions. No compile-time type checks (runtime type check are working fine)
 * Created by darksnake on 28-Jan-17.
 */
public class MultiActionTask extends MultiStageTask<Object> {


    private final String name;
    private final Consumer<TaskModel> modelTransformation;
    private final List<TaskAction> actions;

    public MultiActionTask(String name, Consumer<TaskModel> modelTransformation, List<TaskAction> actions) {
        this.name = name;
        this.modelTransformation = modelTransformation;
        this.actions = actions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void transform(TaskModel model, MultiStageTaskState state) {
        DataNode data = state.getData();
        for (TaskAction action : actions) {
            data = action.apply(model, data);
            if (!action.name.isEmpty()) {
                state.setData(action.name, data);
            }
        }
        state.finish();
    }

    @Override
    protected TaskModel transformModel(TaskModel model) {
        modelTransformation.accept(model);
        return model;
    }

    public MultiActionTask transformModel(Consumer<TaskModel> transform) {
        return new MultiActionTask(
                name,
                modelTransformation.andThen(transform),
                actions
        );
    }

    public MultiActionTask dependsOn(String taskName, Meta taskMeta, String as) {
        return transformModel(model -> model.dependsOn(taskName, taskMeta, as));
    }

    public MultiActionTask dependsOn(String taskName, Meta taskMeta) {
        return transformModel(model -> model.dependsOn(taskName, taskMeta));
    }

    public MultiActionTask dependsOn(String taskName) {
        return transformModel(model -> model.dependsOn(taskName, model.meta().getNodeOrEmpty(taskName)));
    }

    public MultiActionTask dependsOnData(String dataMask, String as) {
        return transformModel(model -> model.data(dataMask, as));
    }

    public MultiActionTask dependsOnData(String dataMask) {
        return transformModel(model -> model.data(dataMask));
    }

    public MultiActionTask doLast(String actionName, Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
        List<TaskAction> newActions = new ArrayList<>(actions);
        newActions.add(new TaskAction(name, actionFactory, metaFactory));
        return new MultiActionTask(
                actionName,
                modelTransformation,
                newActions
        );
    }

    public MultiActionTask doLast(String actionName) {
        return doLast(
                actionName,
                model -> ActionUtils.buildAction(model.getContext(), actionName),
                model -> model.getMeta().getNodeOrEmpty(actionName)
        );
    }

    public MultiActionTask doLast(Action action, Function<TaskModel, Meta> metaFactory) {
        return doLast(
                action.getName(),
                model -> action,
                metaFactory
        );
    }

    public MultiActionTask doFirst(String actionName, Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
        List<TaskAction> newActions = new ArrayList<>(actions);
        newActions.add(0, new TaskAction(name, actionFactory, metaFactory));
        return new MultiActionTask(
                name,
                modelTransformation,
                newActions
        );
    }

    private static class TaskAction {
        String name;
        Function<TaskModel, Action> actionFactory;
        Function<TaskModel, Meta> metaFactory;

        public TaskAction(String name, Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
            this.name = name;
            this.actionFactory = actionFactory;
            this.metaFactory = metaFactory;
        }

        DataNode apply(TaskModel model, DataNode data) {
            return actionFactory.apply(model).run(model.getContext(), data, metaFactory.apply(model));
        }
    }
}
