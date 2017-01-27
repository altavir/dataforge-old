package hep.dataforge.workspace;

import hep.dataforge.data.DataNode;

/**
 * The task that gathers data from workspace and returns it as is.
 * The task configuration is considered to be dependency configuration.
 * No `@gather` node is needed.
 * Created by darksnake on 07-Aug-16.
 */
public class GatherTask<R> extends AbstractTask<R> {
    @Override
    public String getName() {
        return "gather";
    }


    @Override
    protected DataNode<R> run(TaskModel model, DataNode data) {
        return data;
    }

    @Override
    protected TaskModel transformModel(TaskModel model) {
        return model;
    }
}
