package hep.dataforge.workspace.tasks;

import hep.dataforge.actions.Action;
import hep.dataforge.actions.ActionUtils;
import hep.dataforge.actions.GenericAction;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.ContextMetaFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A task defined as a composition of multiple actions. No compile-time type checks (runtime type check are working fine)
 * Created by darksnake on 28-Jan-17.
 */
public class TaskBuilder {

    private String name;
    private Class type = Object.class;
    private BiConsumer<TaskModel.Builder, Meta> modelBuilder = (model, meta) -> {
    };
    private final Map<String, Consumer<DataNode<?>>> listeners = new HashMap<>();
    private final List<TaskAction> actions = new ArrayList<>();

    public TaskBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TaskBuilder type(Class type) {
        this.type = type;
        return this;
    }


    public TaskBuilder updateModel(BiConsumer<TaskModel.Builder, Meta> transform) {
        modelBuilder = modelBuilder.andThen(transform);
        return this;
    }

    /**
     * Add dependency on a specific task
     *
     * @param taskName
     * @return
     */
    public TaskBuilder dependsOn(String taskName) {
        return updateModel((model, meta) -> model.dependsOn(taskName, meta));
    }

    /**
     * Add dependency on specific task using additional meta transformation (or replacement)
     *
     * @param taskName
     * @param transformMeta
     * @return
     */
    public TaskBuilder dependsOn(String taskName, Function<MetaBuilder, Meta> transformMeta) {
        return updateModel((model, meta) -> model.dependsOn(taskName, transformMeta.apply(meta.getBuilder())));
    }

    public TaskBuilder dependsOn(String taskName, String as) {
        return updateModel((model, meta) -> model.dependsOn(taskName, meta, as));
    }

    public TaskBuilder dependsOn(String taskName, String as, Function<MetaBuilder, Meta> transformMeta) {
        return updateModel((model, meta) -> model.dependsOn(taskName, transformMeta.apply(meta.getBuilder()), as));
    }

    public TaskBuilder data(String dataMask) {
        return updateModel((model, meta) -> model.data(dataMask));
    }

    public TaskBuilder data(String dataMask, String as) {
        return updateModel((model, meta) -> model.data(dataMask, as));
    }

    public TaskBuilder dataNode(Class<?> type, String nodeName) {
        return updateModel((model, meta) -> model.dataNode(type, nodeName));
    }

    /**
     * Add last action
     *
     * @param actionFactory action builder
     * @param metaFactory
     * @return
     */
    public TaskBuilder doLast(Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
        actions.add(new TaskAction(actionFactory, metaFactory));
        return this;
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
                TaskModel::getMeta
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
        actions.add(0, new TaskAction(actionFactory, metaFactory));
        return this;
    }

    public TaskBuilder handle(String stage, Consumer<DataNode<?>> handler) {
        if (listeners.containsKey(stage)) {
            listeners.put(stage, listeners.get(stage).andThen(handler));
        } else {
            listeners.put(stage, handler);
        }
        return this;
    }

    private static class TaskAction {
        final Function<TaskModel, Action> actionFactory;
        final Function<TaskModel, Meta> metaFactory;

        TaskAction(Function<TaskModel, Action> actionFactory, Function<TaskModel, Meta> metaFactory) {
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

    public Task<?> build() {
        return new CustomTask(type, name, modelBuilder, actions);
    }


    private static class CustomTask extends MultiStageTask {

        private final String name;
        private final BiConsumer<TaskModel.Builder, Meta> modelBuilder;
        private final Map<String, Consumer<DataNode<?>>> listeners = new HashMap<>();
        private final List<TaskAction> actions;

        public CustomTask(Class type, String name, BiConsumer<TaskModel.Builder, Meta> modelBuilder, List<TaskAction> actions) {
            super(type);
            this.name = name;
            this.modelBuilder = modelBuilder;
            this.actions = actions;
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
        protected void buildModel(TaskModel.Builder model, Meta meta) {
            modelBuilder.accept(model, meta);
        }
    }
}
