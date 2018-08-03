package hep.dataforge.workspace.tasks

import hep.dataforge.actions.Action
import hep.dataforge.actions.ActionUtils
import hep.dataforge.actions.GenericAction
import hep.dataforge.data.DataNode
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.utils.ContextMetaFactory
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

/**
 * A task defined as a composition of multiple actions. No compile-time type checks (runtime type check are working fine)
 * Created by darksnake on 28-Jan-17.
 */
class TaskBuilder(var name: String) {

    private var type: Class<*> = Any::class.java
    private val transformations: MutableList<(TaskModel.Builder, Meta) -> Unit> = ArrayList()
    private val listeners = HashMap<String, Consumer<DataNode<*>>>()
    private val actions = ArrayList<TaskAction>()

    fun name(name: String): TaskBuilder {
        this.name = name
        return this
    }

    fun type(type: Class<out Any>): TaskBuilder {
        this.type = type
        return this
    }


    fun updateModel(transform: (TaskModel.Builder, Meta) -> Unit): TaskBuilder {
        transformations.add(transform)
        return this
    }

    /**
     * Add dependency on a specific task
     *
     * @param taskName
     * @return
     */
    fun dependsOn(taskName: String): TaskBuilder {
        return updateModel { model, meta -> model.dependsOn(taskName, meta) }
    }

    /**
     * Add dependency on specific task using additional meta transformation (or replacement)
     *
     * @param taskName
     * @param transformMeta
     * @return
     */
    fun dependsOn(taskName: String, transformMeta: Function<MetaBuilder, Meta>): TaskBuilder {
        return updateModel({ model, meta -> model.dependsOn(taskName, transformMeta.apply(meta.getBuilder())) })
    }

    fun dependsOn(taskName: String, `as`: String): TaskBuilder {
        return updateModel { model, meta -> model.dependsOn(taskName, meta, `as`) }
    }

    fun dependsOn(taskName: String, `as`: String, transformMeta: Function<MetaBuilder, Meta>): TaskBuilder {
        return updateModel { model, meta -> model.dependsOn(taskName, transformMeta.apply(meta.getBuilder()), `as`) }
    }

    fun data(dataMask: String): TaskBuilder {
        return updateModel { model, meta -> model.data(dataMask) }
    }

    fun data(dataMask: String, `as`: String): TaskBuilder {
        return updateModel { model, meta -> model.data(dataMask, `as`) }
    }

    fun dataNode(type: Class<*>, nodeName: String): TaskBuilder {
        return updateModel { model, meta -> model.dataNode(type, nodeName) }
    }

    /**
     * Add last action
     *
     * @param actionFactory action builder
     * @param metaFactory
     * @return
     */
    fun doLast(actionFactory: (TaskModel) -> Action<in Any, out Any>, metaFactory: (TaskModel) -> Meta): TaskBuilder {
        actions.add(TaskAction(actionFactory, metaFactory))
        return this
    }

    fun doLast(actionName: String): TaskBuilder {
        return doLast(
                { model -> ActionUtils.buildAction<Any, Any>(model.context, actionName) },
                { model -> model.meta.getMetaOrEmpty(actionName) }
        )
    }

    fun doLast(action: Action<in Any, out Any>, metaFactory: (TaskModel) -> Meta): TaskBuilder {
        return doLast(
                { action },
                metaFactory
        )
    }

    /**
     * Append unconfigurable action using task meta as action meta
     *
     * @param action
     * @return
     */
    fun doLast(action: Action<in Any, out Any>): TaskBuilder {
        return doLast(
                { action },
                { it.meta }
        )
    }

    fun doLast(action: Class<Action<in Any, out Any>>): TaskBuilder {
        try {
            return doLast(action.newInstance())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    /**
     * Add joining action as the last one
     *
     * @param factory
     * @return
     */
    fun join(factory: ContextMetaFactory<Function<Map<String, Any>, Any>>): TaskBuilder {
        return doLast(ActionUtils.join(factory))
    }

    /**
     * Add mapping action as the last one
     *
     * @param factory
     * @return
     */
    fun map(factory: ContextMetaFactory<Function<Any, Any>>): TaskBuilder {
        return doLast(ActionUtils.map(factory))
    }

    //TODO add filter

    fun doFirst(actionFactory: (TaskModel) -> Action<in Any, out Any>, metaFactory: (TaskModel) -> Meta): TaskBuilder {
        actions.add(0, TaskAction(actionFactory, metaFactory))
        return this
    }

    fun handle(stage: String, handler: Consumer<DataNode<*>>): TaskBuilder {
        listeners[stage] = listeners[stage]?.andThen(handler) ?: handler
        return this
    }

    private class TaskAction internal constructor(internal val actionFactory: (TaskModel) -> Action<in Any, out Any>, internal val metaFactory: (TaskModel) -> Meta) {

        internal fun buildAction(model: TaskModel): Action<in Any, out Any> {
            return actionFactory(model)
        }

        internal fun buildMeta(model: TaskModel): Meta {
            return metaFactory(model)
        }

        internal fun apply(model: TaskModel, data: DataNode<*>): DataNode<*> {
            return buildAction(model).run(model.context, data, buildMeta(model))
        }
    }

    fun build(): Task<*> {
        return CustomTask(name, actions) { model, meta ->
            transformations.forEach {
                it.invoke(model, meta)
            }
        }
    }


    private class CustomTask(
            override val name: String,
            private val actions: List<TaskAction>,
            private val modelBuilder: (TaskModel.Builder, Meta) -> Unit) : MultiStageTask<Any>(Any::class.java) {
        private val listeners = HashMap<String, (DataNode<*>) -> Unit>()


        override fun transform(model: TaskModel, state: MultiStageTask.MultiStageTaskState): MultiStageTask.MultiStageTaskState {
            var data: DataNode<out Any> = state.data
            for (ta in actions) {
                val action: Action<in Any, out Any> = ta.buildAction(model)
                if (action is GenericAction<*, *>) {
                    data = data.getCheckedNode("", action.inputType)
                    model.logger.debug("Action {} uses type checked node reduction. Working on {} nodes", action.name, data.count(true))
                }
                data = action.run(model.context, data, ta.buildMeta(model))
                if (action.name != ActionUtils.DEFAULT_ACTION_NAME) {
                    state.setData(action.name, data)
                    //handling individual stages result
                    listeners[action.name]?.let { listener ->
                        data.handle(model.context.dispatcher, listener)
                    }
                }
            }
            return state.finish(data)
        }

        override fun buildModel(model: TaskModel.Builder, meta: Meta) {
            modelBuilder(model, meta)
        }
    }
}
