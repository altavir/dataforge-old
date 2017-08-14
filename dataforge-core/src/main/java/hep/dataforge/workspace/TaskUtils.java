/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;
import org.jetbrains.annotations.NotNull;

/**
 * Some utility methods to work with tasks
 *
 * @author Alexander Nozik
 */
public class TaskUtils {

    /**
     * A meta node that is used to add additional dependencies to the task
     * manually
     */
    public static final String GATHER_NODE_NAME = "@gather";

    /**
     * Construct task dependencies using given dependency model.
     *
     * @param model
     */
    public static TaskModel.Builder applyDataModel(TaskModel.Builder model, Meta dataModel) {
        //Iterating over direct data dependancies
        //PENDING replace by DataFactory for unification?
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
        return model;
    }

    public static TaskModel.Builder createDefaultModel(Workspace workspace, String taskName, @NotNull Meta taskMeta) {
        TaskModel.Builder model = new TaskModel.Builder(new TaskModel(workspace, taskName, taskMeta));
        return applyDataModel(model, taskMeta.getMetaOrEmpty(GATHER_NODE_NAME));
    }

    public static DataTree.Builder gather(TaskModel model) {
        DataTree.Builder builder = DataTree.builder();
        model.dependencies().forEach(dep -> {
            dep.apply(builder, model.getWorkspace());
        });
        return builder;
    }
}
