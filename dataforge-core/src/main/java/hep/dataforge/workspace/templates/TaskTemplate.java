package hep.dataforge.workspace.templates;

import hep.dataforge.names.Named;
import hep.dataforge.utils.ContextMetaFactory;
import hep.dataforge.workspace.tasks.Task;

/**
 * A factory to create a task from meta
 */
public interface TaskTemplate extends ContextMetaFactory<Task>, Named {

}
