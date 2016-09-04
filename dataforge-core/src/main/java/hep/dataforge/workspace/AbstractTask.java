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

import hep.dataforge.computation.ProgressCallback;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hep.dataforge.workspace.WorkspaceUtils.gather;

/**
 * Created by darksnake on 21-Aug-16.
 */
public abstract class AbstractTask<R> implements hep.dataforge.workspace.Task {

    @Override
    public DataNode<R> run(TaskModel model) {
        //validate model
        validate(model);
        Context context = model.getWorkspace().getContext();

        hep.dataforge.computation.Task taskProcess = context.taskManager().submit(getName() + "_" + model.hashCode());
        ProgressCallback callback = taskProcess.callback();

        callback.updateTitle(getName());
        callback.updateMessage("Gathering...");

        DataNode input = gather(callback, model).build();
        callback.updateMessage("Evaluating...");

        DataNode<R> output = run(model, callback, input);

        model.outs().forEach(reporter -> {
            output.handle(reporter.getExecutor(), reporter);
        });

        callback.updateMessage("Complete");

        return output;
    }

    protected abstract DataNode<R> run(TaskModel model, ProgressCallback callback, DataNode<?> data);


    @Override
    public void validate(TaskModel model) {
        //TODO add validation here
    }

//    protected Meta getTaskMeta(Context context, TaskModel model) {
//        return model.meta();
//    }

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
}
