/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace

import hep.dataforge.context.Context
import hep.dataforge.data.DataNode
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.tasks.Task
import hep.dataforge.workspace.tasks.TaskModel

/**
 * @author Alexander Nozik
 */
abstract class AbstractWorkspace(override val context: Context) : Workspace {

    abstract val taskMap: Map<String, Task<*>>
    abstract val targetMap: Map<String, Meta>


    override fun optTask(taskName: String): Task<*>? {
        return taskMap[taskName]
    }

    override val tasks: Collection<Task<*>>
        get() = taskMap.values

    override val targets: Collection<Meta>
        get() = targetMap.values

    /**
     * Automatically constructs a laminate if `@parent` value if defined
     * @param name
     * @return
     */
    override fun optTarget(name: String): Meta? {
        val target = targetMap[name]
        return if (target == null) {
            null
        } else {
            if (target.hasValue(PARENT_TARGET_KEY)) {
                Laminate(target, optTarget(target.getString(PARENT_TARGET_KEY)) ?: Meta.empty())
            } else {
                target
            }
        }
    }


    override fun runTask(model: TaskModel): DataNode<*> {
        return getTask(model.name).run(model)
    }

    companion object {

        /**
         * The key in the meta designating parent target. The resulting target is obtained by overlaying parent with this one
         */
        const val PARENT_TARGET_KEY = "@parent"
    }

}
