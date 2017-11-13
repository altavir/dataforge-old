/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.actions;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.PluginDef;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.tables.ReadPointSetAction;
import hep.dataforge.tables.TransformTableAction;
import hep.dataforge.utils.Optionals;
import hep.dataforge.workspace.tasks.Task;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A support manager to dynamically load actions and tasks into the context
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "actions", group = "hep.dataforge", info = "A list of available actions and task for given context")
public class ActionManager extends BasicPlugin {


    private final Map<String, Action> actionMap = new HashMap<>();
    private final Map<String, Task> taskMap = new HashMap<>();

    public ActionManager() {
        //TODO move somewhere else
        putAction(TransformTableAction.class);
        putAction(ReadPointSetAction.class);
        putAction(RunConfigAction.class);
    }

    protected Optional<ActionManager> getParent() {
        if (getContext() == null || getContext().getParent() == null) {
            return Optional.empty();
        } else {
            return getContext().getParent().provide("actions", ActionManager.class);
        }
    }

    @Provides(Action.ACTION_TARGET)
    public Optional<Action> optAction(String name) {
        return Optionals.either(actionMap.get(name))
                .or(getParent().flatMap(parent -> parent.optAction(name)))
                .opt();
    }

    @Provides(Task.TASK_TARGET)
    public Optional<Task> optTask(String name) {
        return Optionals.either(taskMap.get(name))
                .or(getParent().flatMap(parent -> parent.optTask(name)))
                .opt();
    }

    public void put(Action action) {
        if (actionMap.containsKey(action.getName())) {
            LoggerFactory.getLogger(getClass()).warn("Duplicate action names in ActionManager.");
        } else {
            actionMap.put(action.getName(), action);
        }
    }

    public void put(Task task) {
        if (taskMap.containsKey(task.getName())) {
            LoggerFactory.getLogger(getClass()).warn("Duplicate task names in ActionManager.");
        } else {
            taskMap.put(task.getName(), task);
        }
    }

    /**
     * Put a task into the manager using action construction by reflections. Action must have empty constructor
     *
     * @param actionClass a {@link java.lang.Class} object.
     */
    public final void putAction(Class<? extends Action> actionClass) {
        try {
            put(actionClass.newInstance());
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Action must have default empty constructor to be registered.");
        } catch (InstantiationException ex) {
            throw new RuntimeException("Error while constructing Action", ex);
        }
    }

    public final void putTask(Class<? extends Task> taskClass) {
        try {
            put(taskClass.newInstance());
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Task must have default empty constructor to be registered.");
        } catch (InstantiationException ex) {
            throw new RuntimeException("Error while constructing Task", ex);
        }
    }

    /**
     * Stream of all available actions
     *
     * @return
     */
    @ProvidesNames(Action.ACTION_TARGET)
    public Stream<String> getAllActions() {
        return Stream.concat(
                this.actionMap.keySet().stream(),
                getContext().getPluginManager().stream(true)
                        .flatMap(plugin -> plugin.listContent(Action.ACTION_TARGET))
        ).distinct();
    }

    /**
     * Stream of all available tasks
     *
     * @return
     */
    @ProvidesNames(Task.TASK_TARGET)
    public Stream<String> getAllTasks() {
        return Stream.concat(
                this.taskMap.keySet().stream(),
                getContext().getPluginManager().stream(true)
                        .flatMap(plugin -> plugin.listContent(Task.TASK_TARGET))
        ).distinct();
    }

}
