package hep.dataforge.kodex

import hep.dataforge.actions.GenericAction
import hep.dataforge.actions.GroupBuilder
import hep.dataforge.context.Context
import hep.dataforge.data.DataFilter
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.data.NamedData
import hep.dataforge.exceptions.AnonymousNotAlowedException
import hep.dataforge.goals.Goal
import hep.dataforge.io.history.Chronicle
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.names.Name
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.Logger
import java.util.stream.Collectors
import java.util.stream.Stream


class ActionEnv(val context: Context, val name: String, val meta: Meta, val log: Chronicle)


/**
 * Action environment
 */
class PipeBuilder<T, R>(val context: Context, var name: String, var meta: MetaBuilder) {
    lateinit var result: suspend ActionEnv.(T) -> R;

    var logger: Logger = context.getLogger(name)

    /**
     * Calculate the result of goal
     */
    fun result(f: suspend ActionEnv.(T) -> R) {
        result = f;
    }
}

/**
 * Coroutine based pipe action.
 * KPipe supports custom CoroutineContext which allows to override specific way coroutines are created.
 * KPipe is executed inside {@link PipeBuilder} object, which holds name of given data, execution context, meta and log.
 * Notice that name and meta could be changed. Output object receives modified name and meta.
 */
class KPipe<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val action: PipeBuilder<T, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }
        val builder = DataSet.builder(outputType)
        data.dataStream(true).forEach {
            val laminate = Laminate(it.meta, meta)

            val pipe = PipeBuilder<T, R>(
                    context,
                    it.name,
                    laminate.builder
            ).apply(action)

            val env = ActionEnv(
                    context,
                    pipe.name,
                    pipe.meta,
                    context.getChronicle(Name.joinString(pipe.name, name))
            )

            val dispatcher = getExecutorService(context, laminate).asCoroutineDispatcher()

            val goal = it.goal.pipe(dispatcher) {
                pipe.logger.debug("Starting action ${this.name} on ${pipe.name}")
                pipe.result.invoke(env, it).also {
                    pipe.logger.debug("Finished action ${this.name} on ${pipe.name}")
                }
            }
            val res = NamedData(env.name, goal, outputType, env.meta)
            builder.putData(res)
        }

        return builder.build();
    }

    override fun getInputType(): Class<T> {
        return inType ?: super.getInputType()
    }

    override fun getOutputType(): Class<R> {
        return outType ?: super.getOutputType()
    }
}


class JoinGroup<T, R>(val context: Context, name: String? = null, internal val node: DataNode<out T>) {
    var name: String = name ?: node.name;
    var meta: MetaBuilder = node.meta.builder

    lateinit var result: suspend ActionEnv.(Map<String, T>) -> R

    fun result(f: suspend ActionEnv.(Map<String, T>) -> R) {
        this.result = f;
    }

}


class JoinGroupBuilder<T, R>(val context: Context, val meta: Meta) {


    private val groupRules: MutableList<(Context, DataNode<out T>) -> List<JoinGroup<T, R>>> = ArrayList();

    /**
     * introduce grouping by value name
     */
    fun byValue(tag: String, defaultTag: String = "@default", action: JoinGroup<T, R>.() -> Unit) {
        groupRules += { context, node ->
            GroupBuilder.byValue(tag, defaultTag).group(node).map {
                JoinGroup<T, R>(context, null, node).apply(action)
            }
        }
    }

    /**
     * Add a single fixed group to grouping rules
     */
    fun group(groupName: String, filter: DataFilter, action: JoinGroup<T, R>.() -> Unit) {
        groupRules += { context, node ->
            listOf(
                    JoinGroup<T, R>(context, groupName, filter.filter(node)).apply(action)
            )
        }
    }

    /**
     * Apply transformation to the whole node
     */
    fun result(resultName: String? = null, f: suspend ActionEnv.(Map<String, T>) -> R) {
        groupRules += { context, node ->
            listOf(
                    JoinGroup<T, R>(context, resultName, node).apply {
                        result(f)
                        if (resultName != null) {
                            name = resultName
                        }
                    }
            )
        }
    }

    internal fun buildGroups(context: Context, input: DataNode<out T>): Stream<JoinGroup<T, R>> {
        return groupRules.stream().flatMap { it.invoke(context, input).stream() }
    }

}


/**
 * The same rules as for KPipe
 */
class KJoin<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val action: JoinGroupBuilder<T, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }

        val builder = DataSet.builder(outputType)

        JoinGroupBuilder<T, R>(context, meta).apply(action).buildGroups(context, data).forEach { group ->

            val laminate = Laminate(group.meta, meta)

            val goalMap: Map<String, Goal<out T>> = group.node
                    .dataStream()
                    .filter { it.isValid }
                    .collect(Collectors.toMap({ it.name }, { it.goal }))

            val groupName: String = group.name;

            if (groupName.isEmpty()) {
                throw AnonymousNotAlowedException("Anonymous groups are not allowed");
            }

            val env = ActionEnv(
                    context,
                    groupName,
                    laminate.builder,
                    context.getChronicle(Name.joinString(groupName, name))
            )

            val dispatcher = getExecutorService(context, group.meta).asCoroutineDispatcher()

            val goal = goalMap.join(dispatcher) { group.result.invoke(env, it) }
            val res = NamedData(env.name, goal, outputType, env.meta)
            builder.putData(res)
        }

        return builder.build();
    }

    override fun getInputType(): Class<T> {
        return inType ?: super.getInputType()
    }

    override fun getOutputType(): Class<R> {
        return outType ?: super.getOutputType()
    }
}


class SplitBuilder<T, R>(val context: Context) {
    val fragments: MutableList<Pair<String, (String, Meta) -> Pair<String, Meta>>> = ArrayList()
    lateinit var result: suspend ActionEnv.(T) -> Map<String, R>

    /**
     * Calculate the result of goal
     */
    fun result(f: suspend ActionEnv.(T) -> Map<String, R>) {
        result = f;
    }

    /**
     * Add new fragment building rule
     * @param name the name of a fragment
     * @param rule the rule to transform fragment name and meta using
     */
    fun fragment(name: String, rule: (String, Meta) -> Pair<String, Meta>) {
        fragments += Pair(name, rule);
    }
}

class KSplit<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val action: SplitBuilder<T, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }

        val builder = DataSet.builder(outputType)


        runBlocking {
            data.dataStream(true).forEach {

                val laminate = Laminate(it.meta, meta)

                val split = SplitBuilder<T, R>(context).apply(action)

                val env = ActionEnv(
                        context,
                        it.name,
                        laminate.builder,
                        context.getChronicle(Name.joinString(it.name, name))
                )

                val dispatcher = getExecutorService(context, laminate).asCoroutineDispatcher()

                val commonGoal = it.goal.pipe(dispatcher) { split.result.invoke(env, it) }

                split.fragments.forEach { rule ->
                    val goal = commonGoal.pipe(dispatcher) {
                        it[rule.first]!!
                    }
                    val (resName, resMeta) = rule.second.invoke(it.name, laminate)
                    val res = NamedData(resName, goal, outputType, resMeta)
                    builder.putData(res)
                }


            }
        }

        return builder.build();
    }

    override fun getInputType(): Class<T> {
        return inType ?: super.getInputType()
    }

    override fun getOutputType(): Class<R> {
        return outType ?: super.getOutputType()
    }
}