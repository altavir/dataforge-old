package hep.dataforge.workspace;

import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;

/**
 * The task that gathers data from workspace and returns it as is.
 * The task configuration is considered to be dependency configuration.
 * No `@gather` node is needed.
 * Created by darksnake on 07-Aug-16.
 */
public class GatherTask implements Task {
    @Override
    public String getName() {
        return "gather";
    }

    @Override
    public TaskModel build(Workspace workspace, Meta taskConfig) {
        return WorkspaceUtils.applyDataModel(new TaskModel(workspace, getName(), taskConfig), taskConfig);
    }

    @Override
    public void validate(TaskModel model) {
        //Always valid
    }

    @Override
    public DataNode run(TaskModel model) {
        try {
            return model.getWorkspace().getContext().workManager()
                    .post(getName() + "_" + model.hashCode(), callback -> WorkspaceUtils.gather(callback, model).build())
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Exception in gather task", e);
        }
    }
}
