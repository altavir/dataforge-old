/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.ProcessManager;
import hep.dataforge.data.DataTree;

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
}
