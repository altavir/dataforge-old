package hep.dataforge.kodex

import hep.dataforge.actions.GenericAction
import hep.dataforge.context.Context
import hep.dataforge.data.*
import hep.dataforge.exceptions.AnonymousNotAlowedException
import hep.dataforge.goals.Goal
import hep.dataforge.io.history.Chronicle
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.names.Name
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.runBlocking
import java.util.stream.Collectors
import kotlin.coroutines.experimental.CoroutineContext


class ActionEnv(val context: Context, val name: String, val meta: Meta, val log: Chronicle)


/**
 * Action environment
 */
class PipeBuilder<T, R>(val context: Context, var name: String, var meta: MetaBuilder) {
    lateinit var result: suspend ActionEnv.(T) -> R;

    /**
     * Calculate the result of goal
     */
    fun result(f: suspend ActionEnv.(T) -> R) {
        result = f;
    }
}

//TODO move dispatcher to contest

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
        private val dispatcher: CoroutineContext = CommonPool,
        private val action: PipeBuilder<T, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }
        val builder = DataSet.builder(outputType)
        runBlocking {
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

                val goal = it.goal.pipe(dispatcher) { pipe.result.invoke(env, it) }
                val res = NamedData(env.name, goal, outputType, env.meta)
                builder.putData(res)
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


class JoinGroup<T, R>(val context: Context, var name: String?, var meta: MetaBuilder) {
    internal var filter: DataFilter = DataFilter.IDENTITY
    lateinit var result: suspend ActionEnv.(Map<String, T>) -> R

    /**
     * Apply custom filter based on meta
     */
    fun filter(transform: KMetaBuilder.() -> Unit) {
        filter = CustomDataFilter(buildMeta("filter", transform))
    }

    fun filter(meta: Meta) {
        filter = CustomDataFilter(meta)
    }

    fun pattern(pattern: String) {
        filter = DataFilter.byPattern(pattern);
    }

    fun result(f: suspend ActionEnv.(Map<String, T>) -> R) {
        this.result = f;
    }

}


class JoinGroupBuilder<T, R> {
    internal val groups: MutableList<Pair<String?, JoinGroup<T, R>.() -> Unit>> = ArrayList();

    fun group(name: String? = null, spec: JoinGroup<T, R>.() -> Unit) {
        groups += Pair(name, spec);
    }

}


/**
 * The same rules as for KPipe
 */
class KJoin<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val dispatcher: CoroutineContext = CommonPool,
        private val action: JoinGroupBuilder<T, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }

        val builder = DataSet.builder(outputType)

        val groups = JoinGroupBuilder<T, R>().apply(action).groups;

        runBlocking {
            groups.forEach {
                val group = JoinGroup<T, R>(
                        context,
                        it.first,
                        MetaBuilder("meta")
                ).apply(it.second);

                val node = group.filter.filter(data);

                val laminate = Laminate(group.meta, node.meta, meta)

                val goalMap: Map<String, Goal<out T>> = node
                        .dataStream()
                        .filter { it.isValid }
                        .collect(Collectors.toMap({ it.name }, { it.goal }))

                val groupName: String = group.name ?: node.name;

                if (groupName.isEmpty()) {
                    throw AnonymousNotAlowedException("Anonymous groups are not allowed");
                }

                val env = ActionEnv(
                        context,
                        groupName,
                        laminate.builder,
                        context.getChronicle(Name.joinString(groupName, name))
                )


                val goal = goalMap.join(dispatcher) { group.result.invoke(env, it) }
                val res = NamedData(env.name, goal, outputType, env.meta)
                builder.putData(res)
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


class SplitBuilder<T, R>(val context: Context) {
    val fragments: MutableList<Pair<String, (String, Meta) -> Pair<String, Meta>>> = ArrayList()
    lateinit var result: suspend ActionEnv.(T) -> Map<String, R>

    /**
     * Calculate the result of goal
     */
    fun result(f: suspend ActionEnv.(T) -> Map<String, R>) {
        result = f;
    }



}

class KSplit<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val dispatcher: CoroutineContext = CommonPool,
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

                val commonGoal = it.goal.pipe(dispatcher) { split.result.invoke(env, it) }

                split.fragments.forEach { rule ->
                    val goal = commonGoal.pipe {
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