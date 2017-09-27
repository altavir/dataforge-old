package hep.dataforge.kodex

import hep.dataforge.actions.GenericAction
import hep.dataforge.actions.GroupBuilder
import hep.dataforge.context.Context
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.data.NamedData
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

/**
 * Action environment
 */
open class ActionEnv<T, R>(val context: Context, var name: String, val meta: MetaBuilder, val log: Chronicle) {
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
 * KPipe is executed inside {@link ActionEnv} object, which holds name of given data, execution context, meta and log.
 * Notice that name and meta could be changed. Output object receives modified name and meta.
 */
class KPipe<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val dispatcher: CoroutineContext = CommonPool,
        private val action: ActionEnv<T, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }
        val builder = DataSet.builder(outputType)
        runBlocking {
            data.dataStream(true).forEach {
                val laminate = Laminate(it.meta, meta)

                val env = ActionEnv<T, R>(context, it.name, laminate.builder, context.getChronicle(Name.joinString(it.name, name)))
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

//class JoinActionEnv<T, R>(context: Context, name: String, meta: MetaBuilder, log: Chronicle) : ActionEnv<T, R>(context, name, meta, log) {
//    private val groupRules
//}

//TODO add custom grouping rules with result producer for each of them

/**
 * The same rules as for KPipe
 */
class KJoin<T, R>(
        name: String? = null,
        private val inType: Class<T>? = null,
        private val outType: Class<R>? = null,
        private val dispatcher: CoroutineContext = CommonPool,
        private val action: ActionEnv<Map<String, T>, R>.() -> Unit) : GenericAction<T, R>(name) {

    override fun run(context: Context, data: DataNode<out T>, meta: Meta): DataNode<R> {
        if (!this.inputType.isAssignableFrom(data.type())) {
            throw RuntimeException("Type mismatch in action $name. $inputType expected, but ${data.type()} received")
        }

        val builder = DataSet.builder(outputType)

        val groups = GroupBuilder.byMeta(meta).group(data);

        runBlocking {
            groups.forEach {
                val laminate = Laminate(it.meta, meta)

                val goalMap: Map<String, Goal<out T>> = it.dataStream().filter { it.isValid }.collect(Collectors.toMap({ it.name }, { it.goal }))

                val env = ActionEnv<Map<String, T>, R>(context, it.name, laminate.builder, context.getChronicle(Name.joinString(it.name, name)))
                        .apply(action)
                val goal = goalMap.join(dispatcher, env.result)
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
