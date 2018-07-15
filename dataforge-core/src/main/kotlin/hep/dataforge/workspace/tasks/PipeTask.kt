package hep.dataforge.workspace.tasks

import hep.dataforge.actions.OneToOneAction
import hep.dataforge.context.Context
import hep.dataforge.data.DataNode
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta

abstract class PipeTask<T : Any, R : Any> protected constructor(override val name: String, private val inputType: Class<T>, outputType: Class<R>) : AbstractTask<R>(outputType) {

    private val action = PipeAction()


    override fun run(model: TaskModel, data: DataNode<Any>): DataNode<R> {
        return action.run(model.context, data.checked(inputType), model.meta)
    }

    abstract override fun buildModel(model: TaskModel.Builder, meta: Meta)

    protected abstract fun result(context: Context, name: String, input: T, meta: Laminate): R

    private inner class PipeAction : OneToOneAction<T, R>() {

        override val name: String = this@PipeTask.name

        override fun getInputType(): Class<T> {
            return this@PipeTask.inputType
        }

        override fun getOutputType(): Class<R> {
            return this@PipeTask.type
        }

        override fun execute(context: Context, name: String, input: T, inputMeta: Laminate): R {
            return result(context, name, input, inputMeta)
        }
    }
}
