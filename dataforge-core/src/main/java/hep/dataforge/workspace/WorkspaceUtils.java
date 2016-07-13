/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.ProcessManager;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;
import static hep.dataforge.workspace.Task.GATHER_NODE_NAME;

/**
 *
 * @author Alexander Nozik
 */
public class WorkspaceUtils {

//    public static DataTree.Builder gather(ProcessManager.Callback callback, Workspace workspace, TaskModel model) {
//        DataTree.Builder builder = DataTree.builder();
//        callback.setMaxProgress(model.taskDeps().size() + model.dataDeps().size());
//        model.taskDeps().forEach(dep -> {
//            builder.putNode(dep.placementRule(), workspace.runTask(dep.model()));
//            callback.increaseProgress(1);
//        });
//        model.dataDeps().forEach(dep -> {
//            builder.putData(dep.as(), workspace.getData(dep.path()));
//            callback.increaseProgress(1);
//        });
//        return builder;
//    }
    /**
     * Construct task dependencies using given dependency model. Could be
     * extended.
     *
     * @param workspace
     * @param model
     * @param dataModelList
     */
    public static void applyDataModel(Workspace workspace, TaskModel model, Meta dataModel) {
        //Iterating over direct data dependancies
        //PENDING replace by DataFactory for unification?
        dataModel.getNodes("data").stream().forEach((dataElement) -> {
            String dataPath = dataElement.getString("name");
            model.data(dataPath, dataElement.getString("as", dataPath));
        });
        //Iterating over task dependancies
        dataModel.getNodes("task").stream().forEach((taskElement) -> {
            String taskName = taskElement.getString("name");
            Task task = workspace.getTask(taskName);
            //Building model with default data construction
            model.dependsOn(task.buildModel(workspace, taskElement), taskElement.getString("as", taskName));
        });
    }

    //PENDING move to TaskModel constructor?
    public static TaskModel createDefaultModel(Workspace workspace, String taskName, Meta taskMeta) {
        TaskModel model = new TaskModel(workspace, taskName, taskMeta);

        Meta dependacyMeta = Meta.buildEmpty(GATHER_NODE_NAME);
        // Use @gather node for data construction
        if (taskMeta.hasNode(GATHER_NODE_NAME)) {
            dependacyMeta = taskMeta.getNode(GATHER_NODE_NAME);
        }

        applyDataModel(workspace, model, dependacyMeta);

        return model;
    }
}
