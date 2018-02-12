package hep.dataforge.kodex

import hep.dataforge.actions.Action
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataTree
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.workspace.tasks.AbstractTask
import hep.dataforge.workspace.tasks.TaskModel

class KTask(
        private val taskName: String,
        private val modelTransform: TaskModel.Builder.(Meta) -> Unit,
        private val dataTransform: TaskModel.(DataNode<Any>) -> DataNode<Any>
) : AbstractTask<Any>() {

    override fun run(model: TaskModel, data: DataNode<out Any>): DataNode<Any> {
        model.context.logger.info("Starting task '$name' on data node ${data.name} with meta: \n${model.meta}")
        return dataTransform.invoke(model, data.checked(Any::class.java));
    }

    override fun getName(): String {
        return taskName;
    }

    override fun buildModel(model: TaskModel.Builder, meta: Meta) {
        modelTransform.invoke(model, meta);
    }

    //TODO add validation
}

class KTaskBuilder(val name: String) {
    var modelTransform: TaskModel.Builder.(Meta) -> Unit = { data("*") };


    private class DataTransformation<T, R>(
            val inputType: Class<T>,
            val outputType: Class<R>,
            val from: String = "",
            val to: String = "",
            val transform: TaskModel.(DataNode<T>) -> DataNode<R>
    ) {
        fun apply(model: TaskModel, node: DataNode<Any>): DataNode<*> {
            val localData = node.getCheckedNode(from, inputType)
            return transform.invoke(model, localData);
        }
    }

    private val dataTransforms: MutableList<DataTransformation<*, *>> = ArrayList();

    fun model(modelTransform: TaskModel.Builder.(Meta) -> Unit) {
        this.modelTransform = modelTransform
    }

    fun <T, R> transform(inputType: Class<T>, outputType: Class<R>, from: String = "", to: String = "", transform: TaskModel.(DataNode<T>) -> DataNode<R>) {
        dataTransforms += DataTransformation(inputType, outputType, from, to, transform);
    }

    inline fun <reified T, reified R> transform(from: String = "", to: String = "", noinline transform: TaskModel.(DataNode<T>) -> DataNode<R>) {
        transform(T::class.java, R::class.java, from, to, transform)
    }

//    /**
//     * delegate execution to existing task applying model transformation from this builder
//     */
//    fun task(task: Task<*>, from: String = "", to: String = ""){
//        dataTransforms += DataTransformation(to){
//            task.ru
//        }
//    }

    /**
     * Perform given action on data elements in `from` node in input and put the result to `to` node
     */
    inline fun <reified T, reified R> action(action: Action<T, R>, from: String = "", to: String = "") {
        val transform: TaskModel.(DataNode<T>) -> DataNode<R> = { data ->
            action.run(context, data, meta)
        }
        transform(from, to, transform)
    }

    inline fun <reified T, reified R> pipeAction(
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

    inline fun <reified T, reified R> pipe(
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


    inline fun <reified T, reified R> joinAction(
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

    inline fun <reified T, reified R> join(
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

    inline fun <reified T, reified R> splitAction(
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

//    inline fun <reified T, reified R> split(
//            actionName: String = "split",
//            from: String = "",
//            to: String = "",
//            noinline action: suspend ActionEnv.(T) -> Map<String, R>) {
//        val split: Action<T, R> = KSplit(
//                name = Name.joinString(name, actionName),
//                inType = T::class.java,
//                outType = R::class.java,
//                action = {
//                    result(action)
//                }
//        )
//        action(split, from, to);
//    }


    fun build(): KTask {
        val transform: TaskModel.(DataNode<Any>) -> DataNode<Any> = { data ->
            val model = this;
            if (dataTransforms.isEmpty()) {
                //return data node as is
                logger.warn("No transformation present, returning input data")
                data
            } else {
                val builder: DataTree.Builder<Any> = DataTree.builder()
                dataTransforms.forEach { builder.putNode(it.to, it.apply(model, data)) }
                builder.build()
            }
        }
        return KTask(name, modelTransform, transform);
    }
}

fun task(name: String, builder: KTaskBuilder.() -> Unit): KTask {
    return KTaskBuilder(name).apply(builder).build();
}