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
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
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
    public DataNode<T> run(TaskModel m) {
        //apply model transformation to include specific dependancies for this task
        TaskModel model = buildModel(m);
        Workspace workspace = model.getWorkspace();
        ProcessManager manager = workspace.getContext().processManager();
        // root process for this task
        final DFProcess taskProcess = manager.post(getName() + "_" + model.hashCode());

        CompletableFuture<TaskState> gatherTask = manager.<TaskState>post(Name.join(taskProcess.getName(), "gather").toString(),
                callback -> {
                    getLogger().info("Starting gathering phase");
                    Report report = new Report(getName(), workspace.getContext().getReport());
                    return new TaskState(gather(callback, workspace, model), report);
                }).getTask();

        CompletableFuture<TaskState> transformTask = gatherTask
                .thenCompose((TaskState state) -> manager.<TaskState>post(Name.join(taskProcess.getName(), "transform").toString(),
                        callback -> {
                            getLogger().info("Starting transformation phase");
                            TaskState result = transform(callback, workspace.getContext(),
                                    state, getTaskMeta(workspace.getContext(), model));
                            if (!result.isFinished) {
                                getLogger().warn("Task state is not finilized. Using last applyied state as a result");
                                result.finish();
                            }
                            return result;
                        }).getTask());

        CompletableFuture<DataNode<T>> resultTask = transformTask
                .thenCompose((TaskState state) -> manager.<DataNode<T>>post(Name.join(taskProcess.getName(), "result").toString(),
                        callback -> {
                            getLogger().info("Starting report phase");
                            model.outs().forEach(reporter -> {
                                reporter.accept(callback, workspace.getContext(), state);
                            });
                            getLogger().info("Starting result phase");
                            return result(callback, workspace, state, model);
                        }).getTask());

        return resultTask.join();
    }

    /**
     * Apply model transformation to include custom dependencies or change
     * existing ones.
     *
     * @param context
     * @param model
     */
    protected TaskModel buildModel(TaskModel model) {
        return model.copy();
    }

    /**
     * Build new TaskModel and apply specific model transformation for this task.
     * @param workspace
     * @param taskConfig
     * @return 
     */
    @Override
    public TaskModel buildModel(Workspace workspace, Meta taskConfig) {
        return buildModel(new TaskModel(workspace, getName(), taskConfig));
    }    
    
    public Logger getLogger() {
        //TODO replace by context logger
        return LoggerFactory.getLogger(getName());
    }

    protected DataNode gather(ProcessManager.Callback callback, Workspace workspace, TaskModel model) {
        DataTree.Builder builder = DataTree.builder();
        callback.setMaxProgress(model.dependencies().size());
        model.dependencies().forEach(dep -> {
            dep.apply(builder, workspace);
            callback.increaseProgress(1.0);
        });
        return builder.build();
    }

    protected Meta getTaskMeta(Context context, TaskModel model) {
        return model.meta();
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
     * Generating finish and storing it in workspace.
     *
     * @param executor
     * @param workspace
     * @param state
     * @param config
     * @return
     */
    @SuppressWarnings("unchecked")
    protected DataNode<T> result(ProcessManager.Callback callback, Workspace workspace, TaskState state, TaskModel model) {
        workspace.updateStage(getName(), state.getResult());
        return state.getResult();
    }
}
