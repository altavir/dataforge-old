package hep.dataforge.workspace.tasks;

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;

public abstract class PipeTask<T, R> extends AbstractTask<R> {
    private final String name;
    private final Class<T> inputType;
    private final Class<R> outputType;

    private final PipeAction action = new PipeAction();

    protected PipeTask(String name, Class<T> inputType, Class<R> outputType) {
        this.name = name;
        this.inputType = inputType;
        this.outputType = outputType;
    }


    @Override
    protected final DataNode<R> run(TaskModel model, DataNode<?> data) {
        return action.run(model.getContext(), data.checked(inputType), model.getMeta());
    }

    @Override
    protected abstract void buildModel(TaskModel.Builder model, Meta meta);

    protected abstract R result(Context context, String name, T input, Laminate meta);

    @Override
    public final String getName() {
        return name;
    }

    private class PipeAction extends OneToOneAction<T, R> {

        @Override
        public String getName() {
            return PipeTask.this.getName();
        }

        @Override
        public Class<T> getInputType() {
            return inputType;
        }

        @Override
        public Class<R> getOutputType() {
            return outputType;
        }

        @Override
        protected R execute(Context context, String name, T input, Laminate inputMeta) {
            return result(context, name, input, inputMeta);
        }
    }
}
