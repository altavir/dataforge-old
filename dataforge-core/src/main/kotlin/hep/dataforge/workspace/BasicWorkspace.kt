/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace

import hep.dataforge.cache.CachePlugin
import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.data.Data
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataNodeEditor
import hep.dataforge.data.DataTree
import hep.dataforge.kodex.optional
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.tasks.Task
import hep.dataforge.workspace.tasks.TaskModel

/**
 * A basic data caching workspace
 *
 * @author Alexander Nozik
 */
class BasicWorkspace private constructor(
        context: Context,
        override val data: DataNode<*>,
        override val taskMap: Map<String, Task<*>>,
        override val targetMap: Map<String, Meta>) : AbstractWorkspace(context) {

    private val cache: CachePlugin by lazy {
        context.opt(CachePlugin::class.java).optional.orElseGet {
            val pl = CachePlugin(Meta.empty())
            context.pluginManager.load(pl)
            pl
        }
    }

    private fun cacheEnabled(): Boolean {
        return context.getBoolean("immutable.enabled", true)
    }

    override fun runTask(model: TaskModel): DataNode<*> {
        //Cache result if immutable is available and caching is not blocked
        return if (cacheEnabled() && model.meta.getBoolean("immutable.enabled", true)) {
            cache.cacheNode(model.name, model.toMeta(), super.runTask(model))
        } else {
            super.runTask(model)
        }
    }

    override fun clean() {
        logger.info("Cleaning up cache...")
        invalidateCache()
    }

    private fun invalidateCache() {
        if (cacheEnabled()) {
            cache.invalidate()
        }
    }


    class Builder(override var context: Context = Global) : Workspace.Builder {
        private var data: DataNodeEditor<Any> = DataTree.edit(Any::class.java).apply { name = "data" }

        private val taskMap: MutableMap<String, Task<*>> = HashMap<String, Task<*>>()
        private val targetMap: MutableMap<String, Meta> = HashMap()

        //internal var workspace = BasicWorkspace()

        override fun self(): Builder {
            return this
        }

        override fun data(key: String, data: Data<out Any>): Builder {
//            if (this.data.optNode(key) != null) {
//                logger.warn("Overriding non-empty data during workspace data fill")
//            }
            this.data.putData(key, data)
            return self()
        }

        override fun data(key: String?, dataNode: DataNode<out Any>): Builder {
            if (key == null || key.isEmpty()) {
                if (!data.isEmpty) {
                    logger.warn("Overriding non-empty root data node during workspace construction")
                }
                data = dataNode.edit() as DataNodeEditor<Any>
            } else {
                data.putNode(key, dataNode)
            }
            return self()
        }

        override fun task(task: Task<*>): Builder {
            taskMap[task.name] = task
            return self()
        }

        override fun target(name: String, meta: Meta): Builder {
            targetMap[name] = meta
            return self()
        }

        override fun build(): Workspace {
            context.pluginManager.stream(true)
                    .flatMap { plugin -> plugin.provideAll(Task.TASK_TARGET, Task::class.java) }
                    .forEach { taskMap.putIfAbsent(it.name, it) }
            return BasicWorkspace(context, data.build(), taskMap, targetMap)
        }

    }

    companion object {

        fun builder(): Builder {
            return Builder()
        }
    }

}
