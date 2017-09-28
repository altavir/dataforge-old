package hep.dataforge.grind.workspace

import hep.dataforge.actions.Action
import hep.dataforge.data.Data
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.tasks.AbstractTask
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

    //TODO build dependency from closure
    /**
     * Build dependencies from parameter map
     * @param params
     * @return
     */
    private static BiConsumer<TaskModel.Builder, Meta> dependencyBuilder(Map params) {
        //TODO add tests
        return { model, meta ->
            Optional.ofNullable(params.get("data")).ifPresent {
                if (it instanceof List) {
                    it.each { model.data(it as String) }
                } else {
                    model.data(it as String)
                }
            }
            Optional.ofNullable(params.get("dependsOn")).ifPresent {
                model.dependsOn(it as String, meta)
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
                     @DelegatesTo(value = GrindPipe.OneToOneCallable, strategy = Closure.DELEGATE_FIRST) Closure action) {
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
                     @DelegatesTo(value = GrindJoin.ManyToOneCallable, strategy = Closure.DELEGATE_FIRST) Closure action) {
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

    static class CustomTaskSpec {
        final TaskModel model
        final DataNode input
        final DataNode.Builder result = DataSet.builder();

        CustomTaskSpec(TaskModel model, DataNode input) {
            this.model = model
            this.input = input
        }

        void yield(String name, Data data) {
            result.putData(name, data)
        }

        void yield(DataNode node) {
            result.putNode(node)
        }

    }

    static Task custom(Map parameters = [data: "*"], String name,
                       @DelegatesTo(value = CustomTaskSpec, strategy = Closure.DELEGATE_FIRST) Closure cl) {
        return new AbstractTask() {
            @Override
            protected DataNode run(TaskModel model, DataNode dataNode) {
                CustomTaskSpec spec = new CustomTaskSpec(model, dataNode);
                Closure code = cl.rehydrate(spec, null, null)
                code.resolveStrategy = Closure.DELEGATE_ONLY
                code.call()
                return spec.result.build();
            }

            @Override
            protected void buildModel(TaskModel.Builder model, Meta meta) {
                dependencyBuilder(parameters).accept(model, meta)
            }

            @Override
            String getName() {
                return name
            }
        }
    }

    /**
     * Execute external process task
     * @param parameters
     * @param name the name of the task
     * @return
     */
    static Task exec(Map paremeters = [:], String name,
                     @DelegatesTo(value = ExecSpec, strategy = Closure.DELEGATE_ONLY) Closure cl) {
        ExecSpec spec = new ExecSpec();
        spec.actionName = name;
        Closure script = cl.rehydrate(spec, null, null)
        script.setResolveStrategy(Closure.DELEGATE_ONLY)
        script.call()

        Action execAction = spec.build();
        return SingleActionTask.from(execAction, dependencyBuilder(paremeters))
    }
}
