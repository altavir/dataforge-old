package hep.dataforge.workspace;

import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;

/**
 * The task that gathers data from workspace and returns it as is.
 * The task configuration is considered to be dependency configuration.
 * No `@gather` node is needed.
 * Created by darksnake on 07-Aug-16.
 */
public class GatherTask extends AbstractTask<Object> {
    @Override
    public String getName() {
        return "gather";
    }


    @Override
    protected DataNode<Object> run(TaskModel model, DataNode data) {
        return data;
    }

    @Override
    protected void updateModel(TaskModel.Builder model, Meta meta) {
        //do nothing
    }

}
