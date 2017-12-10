package hep.dataforge.kodex

import hep.dataforge.context.Context
import hep.dataforge.goals.Goal
import hep.dataforge.goals.GoalGroup
import hep.dataforge.goals.GoalListener
import hep.dataforge.utils.ReferenceRegistry
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.future.asCompletableFuture
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Coroutine implementation of Goal
 * @param id - string id of the Coal
 * @param deps - dependency goals
 * @param dispatcher custom coroutine dispatcher. By default common pool
 * @param block execution block. Could be suspending
 */
class Coal<R>(
        private val deps: Collection<Goal<*>> = Collections.emptyList(),
        dispatcher: CoroutineContext,
        val id: String = "",
        block: suspend () -> R) : Goal<R> {
    //TODO add Context based CoroutineContext object

    private val listeners = ReferenceRegistry<GoalListener<R>>();

    private val deferred: Deferred<R> = async(dispatcher, CoroutineStart.LAZY) {
        try {
            notifyListeners { onGoalStart() }
            if (!id.isEmpty()) {
                Thread.currentThread().name = "Goal:$id"
            }
            val res = block.invoke()
            notifyListeners { onGoalComplete(res) }
            return@async res
        } catch (ex: Throwable) {
            notifyListeners { onGoalFailed(ex) }
            //rethrow exception
            throw ex
        }
    }

    private fun notifyListeners(action: GoalListener<R>.() -> Unit) {
        listeners.forEach {
            try {
                //TODO use Global dispatch thread here
                action.invoke(it)
            } catch (ex: Exception) {
                LoggerFactory.getLogger(javaClass).error("Failed to notify goal listener", ex)
            }
        }
    }


    suspend fun await(): R {
        run()
        return deferred.await();
    }

    override fun run() {
        deps.forEach { it.run() }
        deferred.start()
    }

    override fun get(): R {
        return runBlocking { await() }
    }

    override fun get(timeout: Long, unit: TimeUnit): R {
        return runBlocking {
            withTimeout(timeout, unit) { await() }
        }
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return deferred.cancel();
    }

    override fun isCancelled(): Boolean {
        return deferred.isCancelled;
    }

    override fun isDone(): Boolean {
        return deferred.isCompleted
    }

    override fun isRunning(): Boolean {
        return deferred.isActive
    }

    override fun result(): CompletableFuture<R> {
        return deferred.asCompletableFuture();
    }

    override fun registerListener(listener: GoalListener<R>) {
        listeners.add(listener, true)
    }

    override fun dependencies(): Stream<Goal<*>> {
        return deps.stream().map { it }
    }
}


fun <R> Context.goal(deps: Collection<Goal<*>> = Collections.emptyList(), id: String = "", block: suspend () -> R): Coal<R> {
    return Coal(deps, coroutineContext, id, block);
}

/**
 * Create a simple generator Coal (no dependencies)
 */
fun <R> Context.generate(id: String = "", block: suspend () -> R): Coal<R> {
    return Coal(Collections.emptyList(), coroutineContext, "", block);
}

/**
 * Join a uniform list of goals
 */
fun <T, R> List<Goal<out T>>.join(dispatcher: CoroutineContext, block: suspend (List<T>) -> R): Coal<R> {
    return Coal(this, dispatcher) {
        block.invoke(this.map {
            it.await()
        })
    }
}

/**
 * Transform using map of goals as a dependency
 */
fun <T, R> Map<String, Goal<out T>>.join(dispatcher: CoroutineContext, block: suspend (Map<String, T>) -> R): Coal<R> {
    return Coal(this.values, dispatcher) {
        block.invoke(this.mapValues { it.value.await() })
    }
}


/**
 * Pipe goal
 */
fun <T, R> Goal<T>.pipe(dispatcher: CoroutineContext, block: suspend (T) -> R): Coal<R> {
    return Coal(listOf(this), dispatcher) {
        block.invoke(this.await())
    }
}

fun Collection<Goal<out Any>>.group(): GoalGroup {
    return GoalGroup(this);
}
