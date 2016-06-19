/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.context.ProcessManager;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The model for task execution. Is computed without actual task invocation.
 * @author Alexander Nozik
 */
public class TaskModel implements Named, Annotated {

    private final String taskName;
    private final Meta taskMeta;
    private final Set<DataDep> dataDeps = new HashSet<>();
    private final Set<TaskDep> taskDeps = new HashSet<>();
    private final Set<TaskOutput> outs = new LinkedHashSet<>();

    public TaskModel(String taskName, Meta taskMeta) {
        this.taskName = taskName;
        this.taskMeta = taskMeta;
    }

    public Set<DataDep> dataDeps() {
        return dataDeps;
    }

    public Set<TaskDep> taskDeps() {
        return taskDeps;
    }

    /**
     * Configuration of accept actions. Could be empty
     *
     * @return
     */
    public Collection<TaskOutput> outs() {
        return outs;
    }

    public TaskOutput out(BiConsumer<Context, TaskState> consumer) {
        TaskOutput out = (ProcessManager.Callback callback, Context context, TaskState state) -> {
            callback.getManager().post(callback.processName() + ".output", () -> consumer.accept(context, state));
        };
        this.outs.add(out);
        return out;
    }

    @Override
    public String getName() {
        return taskName;
    }

    @Override
    public Meta meta() {
        return taskMeta;
    }

    public void dependsOn(TaskModel model, String as) {
        this.taskDeps.add(new TaskDep(model, as));
    }

    public void dependsOn(TaskModel model) {
        this.taskDeps.add(new TaskDep(model, model.getName()));
    }

    public void dependsOnData(String dataPath, String as) {
        this.dataDeps.add(new DataDep(dataPath, as));
    }

    public void dependsOnData(String dataPath) {
        this.dataDeps.add(new DataDep(dataPath, dataPath));
    }

    //TODO make meta configurable
    /**
     * Data dependency
     */
    public static class DataDep {

        String path;

        String as;

        public DataDep(String path, String as) {
            this.path = path;
            this.as = as;
        }

        /**
         * data path in the workspace
         *
         * @return
         */
        public String path() {
            return path;
        }

        /**
         * Name of data dependency in the task data node
         *
         * @return
         */
        public String as() {
            return as;
        }

    }

    /**
     * Task dependency
     */
    public class TaskDep {

        TaskModel taskModel;
        String as;

        public TaskDep(TaskModel taskModel, String as) {
            this.taskModel = taskModel;
            this.as = as;
        }

        /**
         * The model of task
         *
         * @return
         */
        public TaskModel taskModel() {
            return taskModel;
        }

        /**
         * Name of task dependency node
         *
         * @return
         */
        public String as() {
            return as;
        }
    }

    /**
     * Task output
     */
    public interface TaskOutput {
        void accept(ProcessManager.Callback callback, Context context, TaskState state);
    }
}
