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

package hep.dataforge.workspace.tasks;

import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;
import hep.dataforge.workspace.Workspace;


/**
 * Created by darksnake on 21-Aug-16.
 */
public abstract class AbstractTask<R> implements Task<R> {

    private static DataTree.Builder<Object> gather(TaskModel model) {
        DataTree.Builder<Object> builder = DataTree.builder().setName("data");
        model.dependencies().forEach(dep -> {
            dep.apply(builder, model.getWorkspace());
        });
        return builder;
    }

    @Override
    public DataNode<R> run(TaskModel model) {
        //validate model
        validate(model);

        // gather data
        DataNode input = gather(model).build();

        //execute
        DataNode<R> output = run(model, input);

        //handle result
        output.handle(model.getContext().getDispatcher(), this::handle);

        return output;
    }

    protected void handle(DataNode<? super R> output) {
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
     * @param model the model to be transformed
     * @param meta  the whole configuration (not only for this particular task)
     */
    protected abstract void buildModel(TaskModel.Builder model, Meta meta);

    /**
     * Build new TaskModel and apply specific model transformation for this
     * task. By default model uses the meta node with the same node as the name of the task.
     *
     * @param workspace
     * @param meta
     * @return
     */
    @Override
    public TaskModel build(Workspace workspace, Meta meta) {
        Meta taskMeta = meta.getMeta(getName(), meta);
        TaskModel.Builder builder = TaskModel.builder(workspace, getName(), taskMeta);
        buildModel(builder, meta);
        return builder.build();
    }
}
