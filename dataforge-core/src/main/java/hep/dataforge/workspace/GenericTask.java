/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.context.DFProcess;
import hep.dataforge.context.ProcessManager;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.io.reports.Report;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic implementation of task with 4 phases:
 * <ul>
 * <li>gathering</li>
 * <li>transformation</li>
 * <li>reporting</li>
 * <li>finish generation</li>
 * </ul>
 *
 * @author Alexander Nozik
 */
public abstract class GenericTask<T> implements Task<T> {

    @Override
    public DataNode<T> run(Workspace workspace, Meta config) {
        ProcessManager manager = workspace.getContext().processManager();
        // root process for this task
        final DFProcess taskProcess = manager.post(getName() + "_" + config.hashCode());

        CompletableFuture<TaskState> gatherTask = manager.<TaskState>post(Name.join(taskProcess.getName(), "gather").toString(),
                callback -> {
                    getLogger().info("Starting gathering phase");
                    Meta gatherConfig = config.getNode("dependecies", Meta.buildEmpty("dependecies"));
                    Report report = new Report(getName(), workspace.getContext().getReport());
                    return new TaskState(gather(callback, workspace, gatherConfig), report);
                }).getTask();

        CompletableFuture<TaskState> transformTask = gatherTask
                .thenCompose((TaskState state) -> manager.<TaskState>post(Name.join(taskProcess.getName(), "transform").toString(),
                        callback -> {
                            getLogger().info("Starting transformation phase");
                            TaskState result = transform(callback, workspace.getContext(), state, config);
                            if(!result.isFinished){
                                getLogger().warn("Task state is not finilized. Using last applyied state as a result");
                                result.finish();
                            }
                            return result;
                        }).getTask());

        CompletableFuture<DataNode<T>> resultTask = transformTask
                .thenCompose((TaskState state) -> manager.<DataNode<T>>post(Name.join(taskProcess.getName(), "result").toString(),
                        callback -> {
                            getLogger().info("Starting report phase");
                            for (Meta reportMeta : config.getNodes("report")) {
                                report(callback, workspace.getContext(), state, reportMeta);
                            }
                            getLogger().info("Starting result phase");
                            return result(callback, workspace, state, config);
                        }).getTask());

        return resultTask.join();
    }

    public Logger getLogger() {
        //TODO replace by context logger
        return LoggerFactory.getLogger(getName());
    }

    /**
     * Gathering of dependencies from workspace
     *
     * @param executor
     * @param workspace
     * @param gatherConfig
     * @return
     */
    @NodeDef(name = "data", multiple = true, info = "Data dependency element from workspace")
    @NodeDef(name = "task", multiple = true, info = "Task dependecy element from workspace")
    protected DataNode gather(ProcessManager.Callback callback, Workspace workspace, Meta gatherConfig) {
        DataTree.Builder builder = DataTree.builder();
        gatherConfig.getNodes("data").stream().forEach((_item) -> {
            gatherData(builder, workspace, gatherConfig);
        });
        gatherConfig.getNodes("task").stream().forEach((dataElement) -> {
            gatherTask(builder, workspace, dataElement);
        });

        return builder.build();
    }

    @ValueDef(name = "path", required = true)
    @ValueDef(name = "as")
    protected void gatherData(DataTree.Builder builder, Workspace workspace, Meta dataElement) {
        String dataPath = dataElement.getString("path");
        Data data = workspace.getData(dataPath);
        builder.putData(dataElement.getString("as", dataPath), data);
    }

    @ValueDef(name = "name", required = true)
    @ValueDef(name = "as")
    @NodeDef(name = "meta")
    protected void gatherTask(DataTree.Builder builder, Workspace workspace, Meta dataElement) {
        String taskName = dataElement.getString("name");
        Meta taskMeta;
        if (dataElement.hasNode("meta")) {
            if (dataElement.hasValue("meta.from")) {
                taskMeta = workspace.getMeta(dataElement.getString("meta.from"));
            } else {
                taskMeta = dataElement.getNode("meta");
            }
        } else {
            taskMeta = null;
        }
        DataNode taskResult = workspace.runTask(taskName, taskMeta);
        if (dataElement.hasValue("as")) {
            builder.putNode(dataElement.getString("as"), taskResult);
        } else {
            builder.putNode(taskResult);
        }
    }

    /**
     * The main task body
     *
     * @param executor
     * @param state
     * @param config
     */
    protected abstract TaskState transform(ProcessManager.Callback callback, Context context, TaskState state, Meta config);

    /**
     * Reporting task results. No change of state is allowed.
     *
     * @param executor
     * @param state
     * @param config
     */
    protected void report(ProcessManager.Callback callback, Context context, TaskState state, Meta config) {

    }

    /**
     * Generating finish and storing it in workspace.
     *
     * @param executor
     * @param workspace
     * @param state
     * @param config
     * @return
     */
    @SuppressWarnings("unchecked")
    protected DataNode<T> result(ProcessManager.Callback callback, Workspace workspace, TaskState state, Meta config) {
        workspace.updateStage(getName(), state.getResult());
        return state.getResult();
    }

}
