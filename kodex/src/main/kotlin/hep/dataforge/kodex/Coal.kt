package hep.dataforge.kodex

import hep.dataforge.goals.Goal
import hep.dataforge.goals.GoalGroup
import hep.dataforge.goals.GoalListener
import hep.dataforge.utils.ReferenceRegistry
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.future.asCompletableFuture
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Coroutine implementation of Goal
 * @param deps - dependency goals
 * @param dispatcher custom coroutine dispatcher. By default common pool
 * @param block execution block. Could be suspending
 */
class Coal<R>(val deps: Collection<Goal<*>> = Collections.emptyList(), dispatcher: CoroutineContext = CommonPool, block: suspend () -> R) : Goal<R> {
    //TODO add Context based CoroutineContext object

    private val listeners = ReferenceRegistry<GoalListener<R>>();

    private val deferred: Deferred<R> = async(dispatcher, CoroutineStart.LAZY) {
        try {
            //TODO add try-catch for listeners response
            listeners.forEach { it.onGoalStart() }
            val res = block.invoke()
            listeners.forEach { it.onGoalComplete(res) }
            return@async res
        } catch (ex: Throwable) {
            listeners.forEach { it.onGoalFailed(ex) }
            //rethrow exception
            throw ex
        }
    }

    suspend fun await(): R {
        run()
        return deferred.await();
    }

    override fun run() {
        deps.forEach { it.run() }
        deferred.start();
    }

    override fun get(): R {
        run()
        return runBlocking { deferred.await() }
    }

    override fun get(timeout: Long, unit: TimeUnit): R {
        run()
        return runBlocking {
            withTimeout(timeout, unit) {
                deferred.await()
            }
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


/**
 * Join a uniform list of goals
 */
fun <T, R> List<Goal<out T>>.join(dispatcher: CoroutineContext = CommonPool, block: suspend (List<T>) -> R): Coal<R> {
    return Coal<R>(this, dispatcher) {
        block.invoke(this.map {
            it.await()
        })
    }
}

/**
 * Transform using map of goals as a dependency
 */
fun <T, R> Map<String, Goal<out T>>.join(dispatcher: CoroutineContext = CommonPool, block: suspend (Map<String, T>) -> R): Coal<R> {
    return Coal<R>(this.values, dispatcher) {
        block.invoke(this.mapValues { it.value.await() })
    }
}

/**
 * Create a simple generator Coal (no dependencies)
 */
fun <R> generate(dispatcher: CoroutineContext = CommonPool, block: suspend () -> R): Coal<R> {
    return Coal<R>(Collections.emptyList(), dispatcher, block);
}

/**
 * Pipe goal
 */
fun <T, R> Goal<T>.pipe(dispatcher: CoroutineContext = CommonPool, block: suspend (T) -> R): Coal<R> {
    return Coal<R>(listOf(this), dispatcher) {
        block.invoke(this.await())
    }
}

fun Collection<Goal<out Any>>.group(): GoalGroup {
    return GoalGroup(this);
}
