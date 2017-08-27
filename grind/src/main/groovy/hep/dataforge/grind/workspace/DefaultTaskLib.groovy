package hep.dataforge.grind.workspace

import hep.dataforge.actions.Action
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.tasks.SingleActionTask
import hep.dataforge.workspace.tasks.Task
import hep.dataforge.workspace.tasks.TaskModel

import java.util.function.BiConsumer

/**
 * A collection of static methods to create tasks for WorkspaceSpec
 */
class DefaultTaskLib {

//    /**
//     * Create a task using
//     * @param parameters
//     * @param taskName
//     * @param cl
//     * @return
//     */
//    static Task template(Map parameters = [:],
//                         String taskName,
//                         @DelegatesTo(GrindMetaBuilder) Closure cl) {
//        Meta meta = Grind.buildMeta(parameters, taskName, cl);
//        Context context = parameters.getOrDefault("context", Global.instance());
//
//        return StreamSupport.stream(ServiceLoader.load(TaskTemplate).spliterator(), false)
//                .filter { it.name == meta.getName() }
//                .map { it.build(context, meta) }
//                .findFirst().orElseThrow { new NameNotFoundException("Task template with name $taskName not found") }
//    }

    /**
     * Create a task using {@ling TaskSpec}
     * @param taskName
     * @param cl
     * @return
     */
    static Task build(String taskName,
                      @DelegatesTo(value = TaskSpec, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        def taskSpec = new TaskSpec().name(taskName);
        def code = closure.rehydrate(taskSpec, null, null)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code.call()
        return taskSpec.build();
    }

    /**
     * Build dependencies from parameter map
     * @param params
     * @return
     */
    private static BiConsumer<TaskModel.Builder, Meta> dependencyBuilder(Map params) {
        //TODO add tests
        return { model, meta ->
            params.get("data").each {
                model.data(it)
            }
            params.get("dependsOn").each {
                model.dependsOn(it, meta)
            }
        }
    }

    /**
     *  A task with single pipe action defined by {@link GrindPipe}
     * @param params
     * @param name
     * @param action
     * @return
     */
    static Task pipe(Map params = [:],
                     String name,
                     @DelegatesTo(value = GrindPipe.OneToOneCallable, strategy = Closure.DELEGATE_ONLY) Closure action) {
        return SingleActionTask.from(new GrindPipe(params, name, action), dependencyBuilder(params));
    }

    /**
     * A task with single join action defined by {@link GrindJoin}
     * @param params
     * @param name
     * @param action
     * @return
     */
    static Task join(Map params = [:],
                     String name,
                     @DelegatesTo(value = GrindJoin.ManyToOneCallable, strategy = Closure.DELEGATE_ONLY) Closure action) {
        return SingleActionTask.from(new GrindJoin(params, name, action), dependencyBuilder(params));
    }

    /**
     * Create a task from single action using custom dependency builder
     * @param action
     * @return
     */
    static Task action(Map parameters = [:], Action action) {
        return SingleActionTask.from(action, dependencyBuilder(parameters));
    }

    /**
     * Create a single action task using action class reference and custom dependency builder
     * @param action
     * @param dependencyBuilder
     * @return
     */
    static Task action(Map parameters = [:], Class<Action> action) {
        return SingleActionTask.from(action.newInstance(), dependencyBuilder(parameters));
    }

    /**
     * Execute external process task
     * @param parameters
     * @return
     */
    static Task exec(Map parameters){

    }
}
