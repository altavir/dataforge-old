package hep.dataforge.workspace.templates;

import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.workspace.tasks.AbstractTask;
import hep.dataforge.workspace.tasks.Task;
import hep.dataforge.workspace.tasks.TaskModel;

/**
 * The task that gathers data from workspace and returns it as is.
 * The task configuration is considered to be dependency configuration.
 */
public class GatherTaskTemplate implements TaskTemplate {
    @Override
    public String getName() {
        return "gather";
    }

    @Override
    public Task build(Context context, Meta meta) {
        return new AbstractTask() {
            @Override
            protected DataNode run(TaskModel model, DataNode dataNode) {
                return dataNode;
            }

            @Override
            protected void updateModel(TaskModel.Builder model, Meta dataModel) {
                if (dataModel.hasMeta("data")) {
                    dataModel.getMetaList("data").forEach((dataElement) -> {
                        String dataPath = dataElement.getString("name");
                        model.data(dataPath, dataElement.getString("as", dataPath));
                    });
                }
                //Iterating over task dependancies
                if (dataModel.hasMeta("task")) {
                    dataModel.getMetaList("task").forEach((taskElement) -> {
                        String taskName = taskElement.getString("name");
                        Task task = model.getWorkspace().getTask(taskName);
                        //Building model with default data construction
                        model.dependsOn(task.build(model.getWorkspace(), taskElement), taskElement.getString("as", taskName));
                    });
                }
            }

            @Override
            public String getName() {
                return meta.getString("name", GatherTaskTemplate.this.getName());
            }
        };
    }
}
