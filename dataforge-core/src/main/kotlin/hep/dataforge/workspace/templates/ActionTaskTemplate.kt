package hep.dataforge.workspace.templates

import hep.dataforge.actions.Action
import hep.dataforge.actions.ActionUtils.*
import hep.dataforge.context.Context
import hep.dataforge.kodex.toList
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.tasks.MultiStageTask
import hep.dataforge.workspace.tasks.Task
import hep.dataforge.workspace.tasks.TaskModel

class ActionTaskTemplate : TaskTemplate {
    override val name = "actions"

    override fun build(context: Context, meta: Meta): Task<*> {
        val actions = meta.getMetaList(ACTION_NODE_KEY).stream()
                .map { actionMeta -> buildAction<Any, Any>(context, actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE)) }
                .toList()

        return ActionTask(meta.getString("name"), actions)
    }

    private inner class ActionTask(override val name: String, private val actions: List<Action<in Any, out Any>>) : MultiStageTask<Any>(Any::class.java) {

        override fun transform(model: TaskModel, state: MultiStageTask.MultiStageTaskState): MultiStageTask.MultiStageTaskState {
            var res = state.data
            for (action in actions) {
                val actionMeta = model.meta.getMetaOrEmpty(name)
                res = action.run(model.context, res, actionMeta)
                state.setData(action.name, res)
            }
            state.finish(res)
            return state
        }

        override fun buildModel(model: TaskModel.Builder, meta: Meta) {

        }
    }
}
