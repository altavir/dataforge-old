package hep.dataforge.kodex

import hep.dataforge.actions.Action
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataNodeEditor
import hep.dataforge.data.DataTree
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.workspace.tasks.AbstractTask
import hep.dataforge.workspace.tasks.TaskModel

class KTask(
        override val name: String,
        private val modelTransform: TaskModel.Builder.(Meta) -> Unit,
        private val dataTransform: TaskModel.(DataNode<*>) -> DataNode<Any>
) : AbstractTask<Any>() {


    override fun run(model: TaskModel, data: DataNode<*>): DataNode<Any> {
        model.context.logger.info("Starting task '$name' on data node ${data.name} with meta: \n${model.meta}")
        return dataTransform.invoke(model, data);
    }

    override fun buildModel(model: TaskModel.Builder, meta: Meta) {
        modelTransform.invoke(model, meta);
    }

    //TODO add validation
}

class KTaskBuilder(val name: String) {
    var modelTransform: TaskModel.Builder.(Meta) -> Unit = { data("*") };

    private class DataTransformation(
            val from: String = "",
            val to: String = "",
            val transform: (TaskModel, DataNode<*>) -> DataNode<*>
    ) {
        fun apply(model: TaskModel, node: DataNode<Any>): DataNode<*> {
            val localData = if (from.isEmpty()) {
                node
            } else {
                node.getNode(from)
            }
            return transform.invoke(model, localData);
        }
    }

    private val dataTransforms: MutableList<DataTransformation> = ArrayList();

    fun model(modelTransform: TaskModel.Builder.(Meta) -> Unit) {
        this.modelTransform = modelTransform
    }

    fun <T : Any> transform(inputType: Class<T>, from: String = "", to: String = "", transform: TaskModel.(DataNode<T>) -> DataNode<*>) {
        dataTransforms += DataTransformation(from, to) { model: TaskModel, data: DataNode<*> ->
            transform.invoke(model, data.checked(inputType))
        }
    }

    inline fun <reified T : Any> transform(from: String = "", to: String = "", noinline transform: TaskModel.(DataNode<T>) -> DataNode<*>) {
        transform(T::class.java, from, to, transform)
    }

    /**
     * Perform given action on data elements in `from` node in input and put the result to `to` node
     */
    inline fun <reified T : Any, reified R : Any> action(action: Action<T, R>, from: String = "", to: String = "") {
        val transform: TaskModel.(DataNode<T>) -> DataNode<R> = { data ->
            action.run(context, data, meta)
        }
        transform(from, to, transform)
    }

    inline fun <reified T : Any, reified R : Any> pipeAction(
            actionName: String = "pipe",
            from: String = "",
            to: String = "",
            noinline action: PipeBuilder<T, R>.() -> Unit) {
        val pipe: Action<T, R> = KPipe(
                actionName = Name.joinString(name, actionName),
                inType = T::class.java,
                outType = R::class.java,
                action = action
        )
        action(pipe, from, to);
    }

    inline fun <reified T : Any, reified R : Any> pipe(
            actionName: String = "pipe",
            from: String = "",
            to: String = "",
            noinline action: suspend ActionEnv.(T) -> R) {
        val pipe: Action<T, R> = KPipe(
                actionName = Name.joinString(name, actionName),
                inType = T::class.java,
                outType = R::class.java,
                action = {
                    result(action)
                }
        )
        action(pipe, from, to);
    }


    inline fun <reified T : Any, reified R : Any> joinAction(
            actionName: String = "join",
            from: String = "",
            to: String = "",
            noinline action: JoinGroupBuilder<T, R>.() -> Unit) {
        val join: Action<T, R> = KJoin(
                actionName = Name.joinString(name, actionName),
                inType = T::class.java,
                outType = R::class.java,
                action = action
        )
        action(join, from, to);
    }

    inline fun <reified T : Any, reified R : Any> join(
            actionName: String = "join",
            from: String = "",
            to: String = "",
            noinline action: suspend ActionEnv.(Map<String, T>) -> R) {
        val join: Action<T, R> = KJoin(
                actionName = Name.joinString(name, actionName),
                inType = T::class.java,
                outType = R::class.java,
                action = {
                    result(null, action)
                }
        )
        action(join, from, to);
    }

    inline fun <reified T : Any, reified R : Any> splitAction(
            actionName: String = "split",
            from: String = "",
            to: String = "",
            noinline action: SplitBuilder<T, R>.() -> Unit) {
        val split: Action<T, R> = KSplit(
                name = Name.joinString(name, actionName),
                inType = T::class.java,
                outType = R::class.java,
                action = action
        )
        action(split, from, to);
    }


    fun build(): KTask {
        val transform: TaskModel.(DataNode<*>) -> DataNode<Any> = { data ->
            val model = this;
            if (dataTransforms.isEmpty()) {
                //return data node as is
                logger.warn("No transformation present, returning input data")
                data.checked(Any::class.java)
            } else {
                val builder: DataNodeEditor<Any> = DataTree.edit(Any::class.java)
                dataTransforms.forEach {
                    val res = it.apply(model, data as DataNode<Any>)
                    if (it.to.isEmpty()) {
                        builder.update(res)
                    } else {
                        builder.putNode(it.to, res)
                    }
                }
                builder.build()
            }
        }
        return KTask(name, modelTransform, transform);
    }
}

fun task(name: String, builder: KTaskBuilder.() -> Unit): KTask {
    return KTaskBuilder(name).apply(builder).build();
}