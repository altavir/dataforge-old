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

import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;


/**
 * Created by darksnake on 21-Aug-16.
 */
public abstract class AbstractTask<R> implements Task {

    @Override
    public DataNode<R> run(TaskModel model) {
        //validate model
        validate(model);

        // gather data
        DataNode input = TaskUtils.gather(model).build();

        //execute
        DataNode<R> output = run(model, input);

        //handle result
        output.handle(model.getContext().singleThreadExecutor(), this::handle);

        return output;
    }

    protected void handle(DataNode<R> output) {
        //do nothing
    }

    protected abstract DataNode<R> run(TaskModel model, DataNode<?> data);


    @Override
    public void validate(TaskModel model) {
        //TODO add basic validation here
    }


    /**
     * Apply model transformation to include custom dependencies or change
     * existing ones.
     *
     * @param model
     */
    protected abstract TaskModel transformModel(TaskModel model);

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
        return transformModel(TaskUtils.createDefaultModel(workspace, getName(), taskConfig));
    }
}
