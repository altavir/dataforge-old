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
import hep.dataforge.computation.Work;
import hep.dataforge.computation.WorkManager;
import hep.dataforge.data.DataNode;
import hep.dataforge.io.reports.Report;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hep.dataforge.workspace.WorkspaceUtils.gather;

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
    public DataNode<T> run(TaskModel model) {
        //validate model
        validate(model);
        Workspace workspace = model.getWorkspace();
        WorkManager manager = workspace.getContext().workManager();
        // root process for this task
        final Work taskProcess = manager.submit(getName() + "_" + model.hashCode());

        CompletableFuture<TaskState> gatherTask = manager.post(Name.join(taskProcess.getName(), "gather").toString(),
                callback -> {
                    getLogger().debug("Starting gathering phase");
                    Report report = new Report(getName(), workspace.getContext().getReport());
                    return new TaskState(gather(callback, model).build(), report);
                });

        CompletableFuture<TaskState> transformTask = gatherTask
                .thenCompose((TaskState state) -> manager.post(Name.join(taskProcess.getName(), "transform").toString(),
                        callback -> {
                            getLogger().info("Starting transformation phase");
                            TaskState result = transform(callback, workspace.getContext(),
                                    state, getTaskMeta(workspace.getContext(), model));
                            if (!result.isFinished) {
                                getLogger().warn("Task state is not finilized. Using last applyied state as a result");
                                result.finish();
                            }
                            return result;
                        }));

        CompletableFuture<DataNode<T>> resultTask = transformTask
                .thenCompose((TaskState state) -> manager.post(Name.join(taskProcess.getName(), "result").toString(),
                        callback -> {
                            getLogger().info("Starting report phase");
                            model.outs().forEach(reporter -> {
                                reporter.accept(callback, workspace.getContext(), state);
                            });
                            getLogger().info("Starting result phase");
                            return result(callback, workspace, state, model);
                        }));

        return resultTask.join();
    }

    @Override
    public void validate(TaskModel model) {
        //TODO add validation here
    }

    /**
     * Apply model transformation to include custom dependencies or change
     * existing ones.
     *
     * @param model
     */
    protected TaskModel transformModel(TaskModel model) {
        return model;
    }

    /**
     * Build new TaskModel and apply specific model transformation for this
     * task.
     *
     * @param workspace
     * @param taskConfig
     * @return
     */
    @Override
    public TaskModel build(Workspace workspace, Meta taskConfig) {
        return transformModel(WorkspaceUtils.createDefaultModel(workspace, getName(), taskConfig));
    }

    public Logger getLogger() {
        //TODO replace by context logger
        return LoggerFactory.getLogger(getName());
    }

    protected Meta getTaskMeta(Context context, TaskModel model) {
        return model.meta();
    }

    /**
     * The main task body
     *
     * @param state
     * @param config
     */
    protected abstract TaskState transform(WorkManager.Callback callback, Context context, TaskState state, Meta config);

    /**
     * Generating finish and storing it in workspace.
     *
     * @param workspace
     * @param state
     * @return
     */
    protected DataNode<T> result(WorkManager.Callback callback, Workspace workspace, TaskState state, TaskModel model) {
        workspace.updateStage(getName(), state.getResult());
        //FIXME check for type cast
        return (DataNode<T>) state.getResult();
    }
}
