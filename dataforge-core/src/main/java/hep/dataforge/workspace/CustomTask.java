package hep.dataforge.workspace;

import hep.dataforge.actions.Action;
import hep.dataforge.actions.ActionManager;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A customizable task with functionally defined model transformation and run logic.
 * FIXME For evaluation purposes only
 * Created by darksnake on 27-Jan-17.
 */
public class CustomTask extends AbstractTask<Object> {

    /**
     * Create an empty task which uses identity model transformation and identity data transformation
     *
     * @param name
     */
    public static CustomTask empty(String name) {
        return new CustomTask(
                name,
                model -> {
                    return;
                },
                model -> UnaryOperator.identity());
    }

    /**
     * Create a new task using given task as a template
     *
     * @param name
     * @param template
     * @return
     */
    public static CustomTask fromTemplate(String name, AbstractTask template) {
        return new CustomTask(
                name,
                template::transformModel,
                model -> data -> template.run(model, data)
        );
    }

    private final String name;
    private final Consumer<TaskModel> modelTransformation;
    private final Function<TaskModel, UnaryOperator<DataNode>> transformationFactory;

    protected CustomTask(String name, Consumer<TaskModel> modelTransformation, Function<TaskModel, UnaryOperator<DataNode>> transformationFactory) {
        this.name = name;
        this.modelTransformation = modelTransformation;
        this.transformationFactory = transformationFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected DataNode<Object> run(TaskModel model, DataNode<?> data) {
        return transformationFactory.apply(model).apply(data);
    }

    @Override
    protected TaskModel transformModel(TaskModel model) {
        modelTransformation.accept(model);
        return model;
    }

    public CustomTask transformModel(Consumer<TaskModel> transform) {
        return new CustomTask(
                name,
                modelTransformation.andThen(transform),
                transformationFactory
        );
    }

    public CustomTask dependsOn(String taskName, Meta taskMeta, String as) {
        return transformModel(model -> model.dependsOn(taskName, taskMeta, as));
    }

    public CustomTask dependsOn(String taskName, Meta taskMeta) {
        return transformModel(model -> model.dependsOn(taskName, taskMeta));
    }

    public CustomTask dependsOn(String taskName) {
        return transformModel(model -> model.dependsOn(taskName, model.meta().getNodeOrEmpty(taskName)));
    }

    public CustomTask dependsOnData(String dataMask, String as) {
        return transformModel(model -> model.data(dataMask, as));
    }

    public CustomTask dependsOnData(String dataMask) {
        return transformModel(model -> model.data(dataMask));
    }

    /**
     * Compose a new task (this task is not changed) adding given transformation as a last step.
     *
     * @param transform
     * @return
     */
    public CustomTask then(Function<TaskModel, UnaryOperator<DataNode>> transform) {
        return new CustomTask(name,
                modelTransformation,
                model -> data -> transformationFactory.apply(model).andThen(transform.apply(model)).apply(data));
    }

    public CustomTask then(UnaryOperator<DataNode> transform) {
        return new CustomTask(name,
                modelTransformation,
                model -> data -> transformationFactory.apply(model).andThen(transform).apply(data));
    }

    /**
     * Add action with given meta derivation rule
     *
     * @param action
     * @param actionMetaGenerator
     * @return
     */
    public CustomTask then(Action action, Function<TaskModel, Meta> actionMetaGenerator) {
        return new CustomTask(name,
                modelTransformation,
                model -> data -> action.run(
                        model.getContext(),
                        transformationFactory.apply(model).apply(data),
                        actionMetaGenerator.apply(model)
                ));
    }

    /**
     * Add action assuming its configuration is a node in model meta with the same name as action itself
     *
     * @param action
     * @return
     */
    public CustomTask then(Action action) {
        return then(action, model -> model.meta().getNodeOrEmpty(action.getName()));
    }

    public CustomTask then(Class<Action> actionType, Function<TaskModel, Meta> actionMetaGenerator) {
        return new CustomTask(name,
                modelTransformation,
                model -> data -> {
                    Action action = ActionManager.buildFrom(model.getContext()).build(actionType);
                    return action.run(
                            model.getContext(),
                            transformationFactory.apply(model).apply(data),
                            actionMetaGenerator.apply(model)
                    );
                });
    }
}
