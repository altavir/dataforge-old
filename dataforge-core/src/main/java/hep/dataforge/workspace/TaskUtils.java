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
    public static TaskModel applyDataModel(TaskModel model, Meta dataModel) {
        //Iterating over direct data dependancies
        //PENDING replace by DataFactory for unification?
        if (dataModel.hasMeta("data")) {
            dataModel.getMetaList("data").stream().forEach((dataElement) -> {
                String dataPath = dataElement.getString("name");
                model.data(dataPath, dataElement.getString("as", dataPath));
            });
        }
        //Iterating over task dependancies
        if (dataModel.hasMeta("task")) {
            dataModel.getMetaList("task").stream().forEach((taskElement) -> {
                String taskName = taskElement.getString("name");
                Task task = model.getWorkspace().getTask(taskName);
                //Building model with default data construction
                model.dependsOn(task.build(model.getWorkspace(), taskElement), taskElement.getString("as", taskName));
            });
        }
        return model;
    }

    public static TaskModel createDefaultModel(Workspace workspace, String taskName, @NotNull Meta taskMeta) {
        TaskModel model = new TaskModel(workspace, taskName, taskMeta);

        Meta dependencyMeta = Meta.buildEmpty(GATHER_NODE_NAME);
        // Use @gather node for data construction
        if (taskMeta.hasMeta(GATHER_NODE_NAME)) {
            dependencyMeta = taskMeta.getMeta(GATHER_NODE_NAME);
        }

        return applyDataModel(model, dependencyMeta);
    }

    public static DataTree.Builder gather(TaskModel model) {
        DataTree.Builder builder = DataTree.builder();
        model.dependencies().forEach(dep -> {
            dep.apply(builder, model.getWorkspace());
        });
        return builder;
    }

//    /**
//     * Search for a Task in context plugins and up the parent plugin. Throw an exception if action not found.
//     *
//     * @param context
//     * @return
//     */
//    public static Task<?> buildTask(Context context, String taskName) {
//        Path path = Path.of(taskName, Task.TASK_TARGET);
//        return context.pluginManager().stream(true)
//                .map(plugin -> plugin.provide(path, Task.class))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .findFirst()
//                .orElseThrow(() -> new NameNotFoundException(taskName));
//
//    }


//    public static DataTree.Builder gather(TaskModel model, @Nullable Work parentWork) {
//        Work gatherWork = parentWork == null ? model.getContext().getWorkManager().getWork("gather"): parentWork.addChild("gather");
//        DataTree.Builder builder = DataTree.builder();
//        gatherWork.setMaxProgress(model.dependencies().size());
//        model.dependencies().forEach(dep -> {
//            dep.apply(builder, model.getWorkspace());
//            gatherWork.increaseProgress(1.0);
//        });
//        return builder;
//    }
}
