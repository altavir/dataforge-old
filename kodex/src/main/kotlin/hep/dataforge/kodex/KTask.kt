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
        return dataTransform.invoke(model, data.checked(Any::class.java));
    }

    override fun getName(): String {
        return taskName;
    }

    override fun buildModel(model: TaskModel.Builder, meta: Meta) {
        modelTransform.invoke(model, meta);
    }
}

class KTaskBuilder(val name: String) {
    var modelTransform: TaskModel.Builder.(Meta) -> Unit = { data("*") };
    private val dataTransforms: MutableList<DataTransformation> = ArrayList();

    fun model(modelTransform: TaskModel.Builder.(Meta) -> Unit) {
        this.modelTransform = modelTransform
    }

    fun transform(placement: String = "", transform: TaskModel.(DataNode<Any>) -> DataNode<*>) {
        dataTransforms += DataTransformation(placement, transform);
    }

    fun <T, R> action(from: String = "", to: String = "", inputType: Class<T>, action: Action<T, R>) {
        val transform: TaskModel.(DataNode<Any>) -> DataNode<*> = { data ->
            val localData = data.getCheckedNode(from, inputType)
            action.run(context, localData, meta) as DataNode<*>
        }
        dataTransforms += DataTransformation(to, transform)
    }

    inline fun <reified T, R> pipe(from: String = "",
                                   to: String = "",
                                   actionName: String = "pipe",
                                   noinline action: PipeBuilder<T, R>.() -> Unit) {
        val pipe: Action<T, R> = KPipe(name = Name.joinString(name, actionName), action = action);
        action<T, R>(from, to, T::class.java, pipe);
    }

    inline fun <reified T, R> join(from: String = "",
                                   to: String = "",
                                   actionName: String = "join",
                                   noinline action: JoinGroupBuilder<T, R>.() -> Unit) {
        val join: Action<T, R> = KJoin(name = Name.joinString(name, actionName), action = action);
        action<T, R>(from, to, T::class.java, join);
    }

    private class DataTransformation(
            val placement: String = "",
            val transform: TaskModel.(DataNode<Any>) -> DataNode<*>
    )


    internal fun build(): KTask {
        val transform: TaskModel.(DataNode<Any>) -> DataNode<Any> = { data ->
            val model = this;
            val builder: DataTree.Builder<Any> = DataTree.builder()
            dataTransforms.forEach {
                val res = it.transform.invoke(model, data);
                builder.putNode(it.placement, res)
            }
            builder.build()
        }
        return KTask(name, modelTransform, transform);
    }
}

fun task(name: String, builder: KTaskBuilder.() -> Unit): KTask {
    return KTaskBuilder(name).apply(builder).build();
}