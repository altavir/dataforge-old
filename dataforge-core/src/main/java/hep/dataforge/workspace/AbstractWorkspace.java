/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.providers.Path;
import hep.dataforge.workspace.tasks.Task;
import hep.dataforge.workspace.tasks.TaskModel;

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

    /**
     * The key in the meta designating parent target. The resulting target is obtained by overlaying parent with this one
     */
    public static final String PARENT_TARGET_KEY = "@parent";

    protected final Map<String, Task> tasks = new HashMap<>();
    protected final Map<String, Meta> targets = new HashMap<>();
    private Context context;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Task<Object>> optTask(String taskName) {
        if (!tasks.containsKey(taskName)) {
            getLogger().trace("Task with name {} not loaded in workspace. Searching for tasks in the context", taskName);
            List<Task> taskList = getContext().getPluginManager().stream(true)
                    .map(plugin -> plugin.provide(Path.of(Task.TASK_TARGET, taskName), Task.class))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (taskList.isEmpty()) {
                return Optional.empty();
            } else {
                if (taskList.size() > 1) {
                    getLogger().warn("A name conflict during task resolution. " +
                            "Task with name '{}' is present in multiple plugins. " +
                            "Consider loading task explicitly.", taskName);
                }
                return Optional.of(taskList.get(0));
            }
        }
        return Optional.of(tasks.get(taskName));
    }

    @Override
    public Stream<Task> getTasks() {
        return tasks.values().stream();
    }

    @Override
    public Stream<Meta> getTargets() {
        return targets.values().stream();
    }

    /**
     * Automatically constructs a laminate if {@code @parent} value if defined
     * @param name
     * @return
     */
    @Override
    public Optional<Meta> optTarget(String name) {
        Meta target = targets.get(name);
        if(target == null){
            return Optional.empty();
        } else {
            if(target.hasValue(PARENT_TARGET_KEY)){
                return Optional.of(new Laminate(target,optTarget(target.getString(PARENT_TARGET_KEY)).orElse(Meta.empty())));
            } else {
                return Optional.of(target);
            }
        }
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
    public DataNode<Object> runTask(TaskModel model) {
        return getTask(model.getName()).run(model);
    }

}
