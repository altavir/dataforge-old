package hep.dataforge.workspace

import hep.dataforge.context.Context
import hep.dataforge.data.DataNode
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.tasks.Task
import hep.dataforge.workspace.tasks.TaskModel
import java.util.stream.Stream

/**
 * A dynamic workspace can update workspace specification dynamically from external source. It fully delegates all tasks to loaded workspace.
 * Loading new workspace during calculations do not affect current progress because backing workspace is not affected by update.
 */
abstract class DynamicWorkspace : Workspace {


    private var _workspace: Workspace? = null

    /**
     * Get backing workspace instance
     *
     * @return
     */
    protected open val workspace: Workspace
        get() {
            synchronized(this) {
                if (_workspace == null) {
                    _workspace = buildWorkspace()
                }
            }
            return _workspace!!
        }

    /**
     * Check if backing workspace is loaded
     *
     * @return
     */
    protected val isValid: Boolean
        get() = _workspace != null

    override val data: DataNode<*>
        get() = workspace.data

    override val tasks: Stream<Task<*>>
        get() = workspace.tasks

    override val targets: Stream<Meta>
        get() = workspace.targets

    override val context: Context
        get() = workspace.context

    /**
     * Build new workspace instance
     *
     * @return
     */
    protected abstract fun buildWorkspace(): Workspace

    /**
     * Invalidate current backing workspace
     */
    protected fun invalidate() {
        _workspace = null
    }

    override fun getTask(taskName: String): Task<*> {
        return workspace.getTask(taskName)
    }

    override fun optTask(taskName: String): Task<*>? {
        return workspace.optTask(taskName)
    }

    override fun optTarget(name: String): Meta? {
        return workspace.optTarget(name)
    }

    override fun clean() {
        workspace.clean()
    }

    override fun runTask(model: TaskModel): DataNode<*> {
        return workspace.runTask(model)
    }
}
