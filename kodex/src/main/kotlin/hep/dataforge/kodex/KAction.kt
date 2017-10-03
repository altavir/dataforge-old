package hep.dataforge.kodex

import hep.dataforge.actions.GenericAction
import hep.dataforge.context.Context
import hep.dataforge.data.*
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


class ActionEnv(val context: Context, var name: String, val meta: MetaBuilder, val log: Chronicle)


/**
 * Action environment
 */
class PipeEnv<T, R>(val context: Context, var name: String, val meta: MetaBuilder, val log: Chronicle) {
    lateinit var result: suspend (T) -> R;

    /**
     * Calculate the result of goal
     */
    fun result(f: suspend (T) -> R) {
        result = f;
    }
}

//TODO move dispatcher to contest

/**
 * Coroutine based pipe action.
 * KPipe supports custom CoroutineContext which allows to override specific way coroutines are created.
 * KPipe is executed inside {@link PipeEnv} object, which holds name of given data, execution context, meta and log.
 * Notice that name and meta could be changed. Output object receives modified name and meta.
 */
class KPipe<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val dispatcher: CoroutineContext = CommonPool,
        private val action: PipeEnv<T, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }
        val builder = DataSet.builder(outputType)
        runBlocking {
            data.dataStream(true).forEach {
                val laminate = Laminate(it.meta, meta)

                val env = PipeEnv<T, R>(context, it.name, laminate.builder, context.getChronicle(Name.joinString(it.name, name)))
                        .apply(action)
                val goal = it.goal.pipe(dispatcher, env.result)
                val res = NamedData(env.name, goal, outputType, env.meta.build())
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


class JoinGroup<T, R>(name: String?) {
    internal var nameTransform: (DataNode<out T>) -> String = { name ?: it.name }
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

    fun name(transform: (DataNode<out T>) -> String) {
        this.nameTransform = transform
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
                val group = JoinGroup<T, R>(it.first).apply(it.second);

                val node = group.filter.filter(data);

                val laminate = Laminate(node.meta, meta)

                val goalMap: Map<String, Goal<out T>> = node
                        .dataStream()
                        .filter { it.isValid }
                        .collect(Collectors.toMap({ it.name }, { it.goal }))

                val groupName: String = group.nameTransform(node)

                val env = ActionEnv(
                        context,
                        groupName,
                        laminate.builder,
                        context.getChronicle(Name.joinString(groupName, name))
                )


                val goal = goalMap.join(dispatcher) { group.result.invoke(env, it) }
                val res = NamedData(env.name, goal, outputType, env.meta.build())
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
