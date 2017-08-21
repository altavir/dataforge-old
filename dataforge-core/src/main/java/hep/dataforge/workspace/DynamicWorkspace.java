package hep.dataforge.workspace;

import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.workspace.tasks.Task;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A dynamic workspace can update workspace specification dynamically from external source. It fully delegates all tasks to loaded workspace.
 * Loading new workspace during calculations do not affect current progress because backing workspace is not affected by update.
 */
public abstract class DynamicWorkspace implements Workspace {




    private Workspace wsp;

    /**
     * Build new workspace instance
     *
     * @return
     */
    protected abstract Workspace buildWorkspace();

    /**
     * Get backing workspace instance
     *
     * @return
     */
    protected synchronized Workspace getWorkspace() {
        if (wsp == null) {
            wsp = buildWorkspace();
        }
        return wsp;
    }

    /**
     * Invalidate current backing workspace
     */
    protected void invalidate() {
        wsp = null;
    }

    /**
     * Check if backing workspace is loaded
     *
     * @return
     */
    protected boolean isValid() {
        return wsp != null;
    }

    @Override
    public DataNode<Object> getData() {
        return getWorkspace().getData();
    }

    @Override
    public Task<?> getTask(String taskName) {
        return getWorkspace().getTask(taskName);
    }

    @Override
    public Stream<Task> getTasks() {
        return getWorkspace().getTasks();
    }

    @Override
    public Optional<Task<?>> optTask(String taskName) {
        return getWorkspace().optTask(taskName);
    }

    @Override
    public Optional<Meta> optTarget(String name) {
        return getWorkspace().optTarget(name);
    }

    @Override
    public Stream<Meta> getTargets() {
        return getWorkspace().getTargets();
    }

    @Override
    public void clean() {
        getWorkspace().clean();
    }
}
