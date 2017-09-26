package hep.dataforge.kodex

import hep.dataforge.actions.GenericAction
import hep.dataforge.actions.GroupBuilder
import hep.dataforge.context.Context
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.data.NamedData
import hep.dataforge.goals.Goal
import hep.dataforge.io.history.Chronicle
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.names.Name
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.runBlocking
import java.util.stream.Collectors
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Action environment
 */
class ActionEnv(val context: Context, var name: String, val meta: MetaBuilder, val log: Chronicle)

//TODO move dispatcher to contest

/**
 * Coroutine based pipe action.
 * KPipe supports custom CoroutineContext which allows to override specific way coroutines are created.
 * KPipe is executed inside {@link ActionEnv} object, which holds name of given data, execution context, meta and log.
 * Notice that name and meta could be changed. Output object receives modified name and meta.
 */
class KPipe<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val dispatcher: CoroutineContext = CommonPool,
        private val transform: suspend ActionEnv.(T) -> R) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }
        val builder = DataSet.builder(outputType)
        runBlocking {
            data.dataStream(true).forEach {
                val env = ActionEnv(context, it.name, meta.builder, context.getChronicle(Name.joinString(it.name, name)))
                val res = NamedData(env.name, it.goal.pipe(dispatcher) { env.transform(it) }, outputType, env.meta.build())
                builder.putData(res)
            }
        }
        return builder.build();
    }

    override fun getInputType(): Class<T> {
        return inType?:super.getInputType()
    }

    override fun getOutputType(): Class<R> {
        return outType?:super.getOutputType()
    }
}

/**
 * The same rules as for KPipe
 */
open class KJoin<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val dispatcher: CoroutineContext = CommonPool,
        private val transform: suspend ActionEnv.(Map<String, T>) -> R) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }

        val builder = DataSet.builder(outputType)

        val groups = groupBuilder(meta).group(data);

        runBlocking {
            groups.forEach {
                val env = ActionEnv(context, it.name, it.meta.builder, context.getChronicle(Name.joinString(it.name, name)))
                val goalMap: Map<String, Goal<out T>> = it.dataStream().filter { it.isValid }.collect(Collectors.toMap({ it.name }, { it.goal }))
                val goal = goalMap.join(dispatcher) { env.transform(it) }
                val res = NamedData(env.name, goal, outputType, env.meta.build())
                builder.putData(res)
            }
        }
        return builder.build();
    }

    override fun getInputType(): Class<T> {
        return inType?:super.getInputType()
    }

    override fun getOutputType(): Class<R> {
        return outType?:super.getOutputType()
    }

    open fun groupBuilder(meta: Meta): GroupBuilder.GroupRule {
        return GroupBuilder.byMeta(meta);
    }

}
