package hep.dataforge.context

import hep.dataforge.exceptions.ContextLockException
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Lock class for context
 */
class ContextLock(override val context: Context) : ContextAware {
    /**
     * A set of objects that lock this context
     */
    private val lockers = HashSet<Any>()

    val isLocked: Boolean
        get() = !lockers.isEmpty()

    @Synchronized
    fun lock(`object`: Any) {
        this.lockers.add(`object`)
    }

    @Synchronized
    fun unlock(`object`: Any) {
        this.lockers.remove(`object`)
    }

    /**
     * Throws [ContextLockException] if context is locked
     */
    fun tryModify() {
        lockers.stream().findFirst().ifPresent { lock -> throw ContextLockException(lock) }
    }

    /**
     * Apply thread safe lockable object modification
     *
     * @param mod
     */
    @Synchronized
    fun <T> modify(mod: ()->T): T {
        tryModify()
        try {
            return context.dispatcher.submit(mod).get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }

    }

    @Synchronized
    fun modify(mod: () -> Unit) {
        tryModify()
        try {
            context.dispatcher.submit(mod).get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }

    }
}
