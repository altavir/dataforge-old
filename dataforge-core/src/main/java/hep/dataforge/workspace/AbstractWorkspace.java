/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.data.DataNode;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.providers.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractWorkspace implements Workspace {

    protected final Map<String, Task> tasks = new HashMap<>();
    protected final Map<String, Meta> targets = new HashMap<>();
    private Context context;

    @Override
    public Task<?> getTask(String taskName) {
        if (!tasks.containsKey(taskName)) {
            getLogger().trace("Task with name {} not loaded in workspace. Searching for tasks in the context", taskName);
            List<Task> taskList = getContext().pluginManager().stream(true)
                    .map(plugin -> plugin.provide(Path.of(Task.TASK_TARGET, taskName), Task.class))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (taskList.isEmpty()) {
                throw new NameNotFoundException(taskName);
            } else {
                if (taskList.size() > 1) {
                    getLogger().warn("A name conflict during task resolution. " +
                            "Task with name '{}' is present in multiple plugins. " +
                            "Consider loading task explicitly.", taskName);
                }
                return taskList.get(0);
            }
        }
        return tasks.get(taskName);
    }

    @Override
    public Stream<Task> getTasks() {
        return tasks.values().stream();
    }

    @Override
    public Stream<Meta> getTargets() {
        return targets.values().stream();
    }

    @Override
    public Meta getTarget(String name) {
        if (!targets.containsKey(name)) {
            throw new NameNotFoundException(name);
        }
        return targets.get(name);
    }

    @Override
    public boolean hasTarget(String name) {
        return targets.containsKey(name);
    }

    @Override
    public Context getContext() {
        if (context == null) {
            return Global.getDefaultContext();
        }
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
    }


    @Override
    public DataNode<?> runTask(TaskModel model) {
        return getTask(model.getName()).run(model);
    }

}
