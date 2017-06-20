package hep.dataforge.workspace;

import hep.dataforge.actions.Action;
import hep.dataforge.actions.ActionUtils;
import hep.dataforge.actions.GenericAction;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.ContextMetaFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A task defined as a composition of multiple actions. No compile-time type checks (runtime type check are working fine)
 * Created by darksnake on 28-Jan-17.
 */
public class TaskBuilder<T> extends MultiStageTask<T> {

    @NotNull
    public static <T> TaskBuilder<T> build(String name, Class<T> type) {
        return new TaskBuilder<>(name, type);
    }

    @NotNull
    public static TaskBuilder<?> build(String name) {
        return new TaskBuilder<>(name, Object.class);
    }


    private final String name;
    private Function<TaskModel, TaskModel> modelTransformation;
    private Map<String, Consumer<DataNode<?>>> listeners = new HashMap<>();
    private List<TaskAction> actions;


    /**
     * An empty task that could be built upon
     *
     * @param name
     */
    private TaskBuilder(String name, Class<T> type) {
        super(type);
        this.name = name;
        modelTransformation = UnaryOperator.identity();
        actions = new ArrayList<>();
    }

    private TaskBuilder<T> copy(Consumer<TaskBuilder> cons) {
        TaskBuilder<T> res = new TaskBuilder<>(name, type);
        res.modelTransformation = this.modelTransformation;
        res.actions = this.actions;
        res.listeners = this.listeners;
        cons.accept(res);
        return res;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected MultiStageTaskState transform(TaskModel model, MultiStageTaskState state) {
        DataNode data = state.getData();
        for (TaskAction ta : actions) {
            Action<?, ?> action = ta.buildAction(model);
            if (action instanceof GenericAction) {
                data = data.getCheckedNode("", ((GenericAction) action).getInputType());
                model.getLogger().debug("Action {} uses type checked node reduction. Working on {} nodes", action.getName(), data.dataSize(true));
            }
            data = action.run(model.getContext(), data, ta.buildMeta(model));
            if (!action.getName().equals(ActionUtils.DEFAULT_ACTION_NAME)) {
                state.setData(action.getName(), data);
                //handling individual stages result
                if (listeners.containsKey(action.getName())) {
                    data.handle(model.getContext().singleThreadExecutor(), listeners.get(action.getName()));
                }
            }
        }
        return state.finish(data);
    }

    @Override
    protected TaskModel transformModel(TaskModel model) {
        return modelTransformation.apply(model);
    }

    public TaskBuilder transformModel(Function<TaskModel, TaskModel> transform) {
        return copy(tb -> tb.modelTransformation = modelTransformation.andThen(transform));
    }

    public TaskBuilder dependsOn(String taskName, Meta taskMeta, String as) {
        return transformModel(model -> model.dependsOn(taskName, taskMeta, as));
    }

    public TaskBuilder dependsOn(String taskName, Meta taskMeta) {
        return transformModel(model -> model.dependsOn(taskName, taskMeta));
    }

    public TaskBuilder dependsOn(String taskName) {
        return transformModel(model -> model.dependsOn(taskName, model.meta().getMetaOrEmpty(taskName)));
    }

    public TaskBuilder dependsOnData(String dataMask, String as) {
        return transformModel(model -> model.data(dataMask, as));
    }

    public TaskBuilder dependsOnData(String dataMask) {
        return transformModel(model -> model.data(dataMask));
    }

    public TaskBuilder dependsOnDataNode(Class<?> type, String nodeName) {
        return transformModel(model -> model.dataNode(type, nodeName));
    }

    /**
     * Add last action
     *
     * @param actionFactory action builder
     * @param metaFactory
     * @return
     */
    public TaskBuilder doLast(Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
        List<TaskAction> newActions = new ArrayList<>(actions);
        newActions.add(new TaskAction(actionFactory, metaFactory));
        return copy(tb -> tb.actions = newActions);
    }

    public TaskBuilder doLast(String actionName) {
        return doLast(
                model -> ActionUtils.buildAction(model.getContext(), actionName),
                model -> model.getMeta().getMetaOrEmpty(actionName)
        );
    }

    public TaskBuilder doLast(Action action, Function<TaskModel, Meta> metaFactory) {
        return doLast(
                model -> action,
                metaFactory
        );
    }

    /**
     * Append unconfigurable action using task meta as action meta
     *
     * @param action
     * @return
     */
    public TaskBuilder doLast(Action action) {
        return doLast(
                model -> action,
                model -> model.meta()
        );
    }

    public TaskBuilder doLast(Class<Action> action) {
        try {
            return doLast(action.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add joining action as the last one
     *
     * @param factory
     * @return
     */
    public TaskBuilder join(ContextMetaFactory<Function<Map<String, Object>, Object>> factory) {
        return doLast(ActionUtils.join(factory));
    }

    /**
     * Add mapping action as the last one
     *
     * @param factory
     * @return
     */
    public TaskBuilder map(ContextMetaFactory<Function<Object, Object>> factory) {
        return doLast(ActionUtils.map(factory));
    }

    //TODO add filter

    public TaskBuilder doFirst(Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
        List<TaskAction> newActions = new ArrayList<>(actions);
        newActions.add(0, new TaskAction(actionFactory, metaFactory));
        return copy(tb -> tb.actions = newActions);
    }

    public TaskBuilder handle(String stage, Consumer<DataNode<?>> handler) {
        HashMap<String, Consumer<DataNode<?>>> newListeners = new HashMap<>(listeners);
        if (newListeners.containsKey(stage)) {
            newListeners.put(stage, newListeners.get(stage).andThen(handler));
        } else {
            newListeners.put(stage, handler);
        }
        return copy(tb -> tb.listeners = newListeners);
    }

    private static class TaskAction {
        final Function<TaskModel, Action> actionFactory;
        final Function<TaskModel, Meta> metaFactory;

        public TaskAction(Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
            this.actionFactory = actionFactory;
            this.metaFactory = metaFactory;
        }

        Action<?, ?> buildAction(TaskModel model) {
            return actionFactory.apply(model);
        }

        Meta buildMeta(TaskModel model) {
            return metaFactory.apply(model);
        }

        DataNode apply(TaskModel model, DataNode data) {
            return buildAction(model).run(model.getContext(), data, buildMeta(model));
        }
    }
}
