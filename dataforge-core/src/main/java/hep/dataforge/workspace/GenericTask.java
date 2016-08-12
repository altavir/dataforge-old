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

import hep.dataforge.computation.Work;
import hep.dataforge.computation.WorkManager;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.io.reports.Report;
import hep.dataforge.meta.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hep.dataforge.workspace.WorkspaceUtils.gather;

/**
 * A generic implementation of task with 4 phases:
 * <ul>
 * <li>gathering</li>
 * <li>transformation</li>
 * <li>reporting</li>
 * <li>result generation</li>
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

//        CompletableFuture<Void> process = new CompletableFuture<>();
        Work taskProcess = workspace.getContext().workManager().submit(getName() + "_" + model.hashCode());
        WorkManager.Callback callback = taskProcess.callback();

        getLogger().debug("Starting gathering phase");
        callback.updateTitle(getName());
        callback.updateMessage("Gathering...");

        Report report = new Report(getName(), workspace.getContext().getReport());
        TaskState state = new TaskState(gather(callback, model).build(), report);

        getLogger().debug("Starting transformation phase");
        callback.updateMessage("Data transformation...");
        transform(callback, workspace.getContext(), state, getTaskMeta(workspace.getContext(), model));
        if (!state.isFinished) {
            getLogger().warn("Task state is not finalized. Using last applied state as a result");
            state.finish();
        }

        model.outs().forEach(reporter -> {
            reporter.accept(workspace.getContext(), state);
        });

        getLogger().debug("Starting result phase");

        callback.updateMessage("Task result generation...");
        DataNode<T> res = result(workspace, state, model);

        callback.updateMessage("Complete");
//        process.complete(null);
        return res;
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
    protected abstract void transform(WorkManager.Callback callback, Context context, TaskState state, Meta config);

    /**
     * Generating finish and storing it in workspace.
     *
     * @param workspace
     * @param state
     * @return
     */
    protected DataNode<T> result(Workspace workspace, TaskState state, TaskModel model) {
        workspace.updateStage(getName(), state.getResult());
        //FIXME check for type cast
        return (DataNode<T>) state.getResult();
    }
}
