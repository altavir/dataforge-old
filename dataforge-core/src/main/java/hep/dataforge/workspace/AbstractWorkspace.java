/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractWorkspace implements Workspace {

    private Context context;
    protected final Map<String, Task> tasks = new HashMap<>();
    protected final Map<String, Meta> metas = new HashMap<>();

    @Override
    public <T> Task<T> getTask(String taskName) {
        if(! tasks.containsKey(taskName)){
            throw new NameNotFoundException(taskName);
        }
        return tasks.get(taskName);
    }

    @Override
    public Meta getMeta(String name) {
        return metas.get(name);
    }

    @Override
    public Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
    }


//    /**
//     * Gathering of dependencies from workspace
//     *
//     * @param executor
//     * @param workspace
//     * @param gatherConfig
//     * @return
//     */
//    @NodeDef(name = "data", multiple = true, info = "Data dependency element from workspace")
//    @NodeDef(name = "task", multiple = true, info = "Task dependecy element from workspace")
//    protected DataTree.Builder gather(ProcessManager.Callback callback, Workspace workspace, Meta gatherConfig) {
//        DataTree.Builder builder = DataTree.builder();
//        gatherConfig.getNodes("data").stream().forEach((dataElement) -> {
//            gatherData(builder, workspace, dataElement);
//        });
//        gatherConfig.getNodes("task").stream().forEach((dataElement) -> {
//            gatherTask(builder, workspace, dataElement);
//        });
//
//        return builder;
//    }
//
//    @ValueDef(name = "path", required = true)
//    @ValueDef(name = "as")
//    protected void gatherData(DataTree.Builder builder, Workspace workspace, Meta dataElement) {
//        String dataPath = dataElement.getString("path");
//        Data data = workspace.getData(dataPath);
//        builder.putData(dataElement.getString("as", dataPath), data);
//    }
//
//    @ValueDef(name = "name", required = true)
//    @ValueDef(name = "as")
//    @NodeDef(name = "meta")
//    protected void gatherTask(DataTree.Builder builder, Workspace workspace, Meta dataElement) {
//        String taskName = dataElement.getString("name");
//        Meta taskMeta;
//        if (dataElement.hasNode("meta")) {
//            if (dataElement.hasValue("meta.from")) {
//                taskMeta = workspace.getMeta(dataElement.getString("meta.from"));
//            } else {
//                taskMeta = dataElement.getNode("meta");
//            }
//        } else {
//            taskMeta = null;
//        }
//        //TODO check cyclic dependacies
//        DataNode taskResult = workspace.runTask(taskName, taskMeta);
//        if (dataElement.hasValue("as")) {
//            builder.putNode(dataElement.getString("as"), taskResult);
//        } else {
//            builder.putNode(taskResult);
//        }
//    }


}
