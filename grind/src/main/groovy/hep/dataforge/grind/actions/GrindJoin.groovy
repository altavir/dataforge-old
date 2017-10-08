package hep.dataforge.grind.actions

import hep.dataforge.actions.GenericAction
import hep.dataforge.actions.GroupBuilder
import hep.dataforge.context.Context
import hep.dataforge.data.DataFilter
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.data.NamedData
import hep.dataforge.goals.AbstractGoal
import hep.dataforge.goals.Goal
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder

import java.util.concurrent.Executor
import java.util.function.BiFunction
import java.util.stream.Stream

//@CompileStatic
class GrindJoin<T, R> extends GenericAction<T, R> {
    static GrindJoin build(Map params = [:], String name, @DelegatesTo(GrindJoin.JoinGroupBuilder) Closure action) {
        Class inputType = params.get("inputType",Object) as Class
        Class outputType = params.get("outputType",Object) as Class
        return new GrindJoin<>(name, inputType, outputType, action);
    }


    private final Class<T> inputType;
    private final Class<R> outputType;
    private final Closure action;

    GrindJoin(String name, Class<T> inputType = null, Class<R> outputType = null,
              @DelegatesTo(JoinGroupBuilder) Closure action) {
        super(name)
        this.inputType = inputType
        this.outputType = outputType
        this.action = action
    }

    @Override
    Class<T> getInputType() {
        return inputType ?: super.inputType;
    }

    @Override
    Class<R> getOutputType() {
        return outputType ?: super.outputType;
    }

    @Override
    DataNode<R> run(Context context, DataNode<? extends T> data, Meta actionMeta) {
        //applying DSL
        JoinGroupBuilder<T, R> groupBuilder = new JoinGroupBuilder<>();
        def rehydrated = action.rehydrate(groupBuilder, groupBuilder, groupBuilder);
        rehydrated.run();

        //adding data
        DataSet.Builder<R> dataBuilder = DataSet.builder(outputType).setName(data.name).setMeta(data.meta);
        groupBuilder.groupRules.collectMany { BiFunction<Context, DataNode<? extends T>, List<JoinGroup<T, R>>> f ->
            f.apply(context, data).collect()
        }.forEach { JoinGroup<T, R> group ->
            dataBuilder.putData(runOne(group))
        }

        return dataBuilder.build();
    }

    private NamedData<R> runOne(JoinGroup<T, R> group) {
        def executor = buildExecutor(group.context, group.meta);
        def goal = new GrindJoinGoal(executor, group)
        return new NamedData<R>(group.name, goal, outputType, group.meta)
    }


    private class GrindJoinGoal extends AbstractGoal<R> {

        JoinGroup<T, R> group;

        GrindJoinGoal(Executor executor, JoinGroup<T, R> group) {
            super(executor)
            this.group = group
        }

        @Override
        protected boolean failOnError() {
            return true;
        }

        @Override
        Stream<Goal<?>> dependencies() {
            return group.node.nodeGoal().dependencies();
        }

        @Override
        protected R compute() throws Exception {
//            Thread.currentThread().setName(Name.joinString(getThreadName(actionMeta), group.getName()));
            // In this moment, all the data is already calculated
            Map<String, T> collection = group.node.collectEntries { [(it.name): it.get()] }
            R res = group.result.call(collection);
            return res;
        }

    }

    class JoinGroup<T, R> {
        final Context context
        private final DataNode<? extends T> node;

        String name = node.name;
        MetaBuilder meta = node.meta.builder;

        Closure<R> result = { it };

        JoinGroup(Context context, DataNode<? extends T> node) {
            this.context = context
            this.node = node
        }

        void result(@DelegatesTo(value = ActionEnv, strategy = Closure.DELEGATE_FIRST) Closure<R> result) {
            this.result = result;
        }

    }

    class JoinGroupBuilder<T, R> {
        private List<BiFunction<Context, DataNode<? extends T>, List<JoinGroup<T, R>>>> groupRules = new ArrayList<>();

        /**
         * introduce grouping by value name
         */
        void byValue(String tag, String defaultTag = "@default", @DelegatesTo(JoinGroup) Closure action) {
            groupRules += { Context context, DataNode<T> node ->
                GroupBuilder.byValue(tag, defaultTag).group(node).collect {
                    def group = new JoinGroup<T, R>(context, node)
                    action.setDelegate(group)
                    action.setResolveStrategy(Closure.DELEGATE_FIRST)
                    action.run()
                    return group
                }
            }
        }

        /**
         * Add a single fixed group to grouping rules
         */
        void group(String groupName, DataFilter filter, Closure action) {
            groupRules += { Context context, DataNode<T> node ->
                def group = new JoinGroup<T, R>(context, filter.filter(node))
                action.setDelegate(group)
                action.setResolveStrategy(Closure.DELEGATE_FIRST)
                action.run()
                return group
            }
        }

        /**
         * Apply transformation to the whole node
         */
        void result(String resultName = getName(), Closure<R> result) {
            groupRules += { Context context, DataNode<T> node ->
                def group = new JoinGroup<T, R>(context, node)
                group.result(result)
                if (resultName != null) {
                    group.name = resultName
                }
                return group
            }
        }

    }


}
