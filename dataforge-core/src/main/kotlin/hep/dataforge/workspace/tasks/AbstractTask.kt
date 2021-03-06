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

package hep.dataforge.workspace.tasks

import hep.dataforge.data.DataNode
import hep.dataforge.data.DataNodeEditor
import hep.dataforge.data.DataTree
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.Workspace


/**
 * Created by darksnake on 21-Aug-16.
 */
abstract class AbstractTask<R: Any> : Task<R> {

    private fun gather(model: TaskModel): DataNodeEditor<*> {
        val builder: DataNodeEditor<Any> = DataTree.edit().apply { name = "data" }
        model.dependencies.forEach { dep ->
            dep.apply(builder, model.workspace)
        }
        return builder
    }

    override fun run(model: TaskModel): DataNode<out R> {
        //validate model
        validate(model)

        // gather data
        val input = gather(model).build()

        //execute
        val output = run(model, input)

        //handle result
        output.handle(model.context.dispatcher) { this.handle(it) }

        return output
    }

    protected fun handle(output: DataNode<out R>) {
        //do nothing
    }

    protected abstract fun run(model: TaskModel, data: DataNode<*>): DataNode<out R>


    override fun validate(model: TaskModel) {
        //TODO add basic validation here
    }


    /**
     * Apply model transformation to include custom dependencies or change
     * existing ones.
     *
     * @param model the model to be transformed
     * @param meta  the whole configuration (not only for this particular task)
     */
    protected abstract fun buildModel(model: TaskModel.Builder, meta: Meta)

    /**
     * Build new TaskModel and apply specific model transformation for this
     * task. By default model uses the meta node with the same node as the name of the task.
     *
     * @param workspace
     * @param taskConfig
     * @return
     */
    override fun build(workspace: Workspace, taskConfig: Meta): TaskModel {
        val taskMeta = taskConfig.getMeta(name, taskConfig)
        val builder = TaskModel.builder(workspace, name, taskMeta)
        buildModel(builder, taskConfig)
        return builder.build()
    }
}
