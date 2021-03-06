/*
 * Copyright  2017 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.control.ports

import hep.dataforge.Named
import hep.dataforge.exceptions.PortException
import hep.dataforge.exceptions.PortLockException
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaHolder
import hep.dataforge.meta.MetaID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock


/**
 * The controller which is currently working with this handler. One
 * controller can simultaneously hold many handlers, but handler could be
 * held by only one controller.
 */
interface PortController {

    fun accept(byte: Byte)

    fun accept(bytes: ByteArray)

    fun error(errorMessage: String, error: Throwable) {
        //do nothing
    }
}


/**
 * The universal asynchronous port handler
 *
 * @author Alexander Nozik
 */
abstract class Port(meta: Meta) : MetaHolder(meta), AutoCloseable, MetaID, Named {

    private val portLock = ReentrantLock(true)

    private var controller: PortController? = null

    protected val executor = Executors.newSingleThreadExecutor { r ->
        val res = Thread(r)
        res.name = "port::$name"
        res.priority = Thread.MAX_PRIORITY
        res
    }

    protected val logger: Logger by lazy { LoggerFactory.getLogger(meta.getString("logger", "port.$name")) }

    abstract val isOpen: Boolean

    private val isLocked: Boolean
        get() = this.portLock.isLocked
//
//    fun setPhraseCondition(condition: (String) -> Boolean) {
//        this.phraseCondition = condition
//    }
//
//    fun setDelimiter(delimiter: String) {
//        phraseCondition = { str: String -> str.endsWith(delimiter) }
//    }

    @Throws(PortException::class)
    abstract fun open()

    /**
     * Run something on port thread
     * @param r
     */
    protected fun execute(r: () -> Unit) {
        executor.submit(r)
    }

    /**
     * Emergency hold break.
     */
    @Synchronized
    fun breakHold() {
        if (isLocked) {
            logger.warn("Breaking hold on port $name")
            execute { portLock.unlock() }
        }
    }

    /**
     * An unique ID for this port
     *
     * @return
     */
    override fun toString(): String {
        return name
    }

    /**
     * Acquire lock on this instance of port handler with given controller
     * object. If port is currently locked by another controller, the wait until it is released.
     * Only the same controller  can release the port.
     *
     * @param controller
     * @throws hep.dataforge.exceptions.PortException
     */
    @Throws(PortException::class)
    fun holdBy(controller: PortController?) {
        assert(controller != null)
        if (!isOpen) {
            open()
        }

        execute {
            try {
                portLock.lockInterruptibly()
            } catch (ex: InterruptedException) {
                logger.error("Lock on port {} is broken", toString())
                throw RuntimeException(ex)
            }
        }
        logger.debug("Locked by {}", controller)
        this.controller = controller
    }


    /**
     * Receive a single byte
     */
    fun receive(byte: Byte) {
        controller?.accept(byte)
    }

    /**
     * Receive an array of bytes
     */
    fun receive(bytes: ByteArray) {
        controller?.accept(bytes)
    }

    /**
     * send the message to the port
     *
     * @param message
     * @throws hep.dataforge.exceptions.PortException
     */
    @Throws(PortException::class)
    protected abstract fun send(message: String)

    /**
     * Send the message if the controller is correct
     *
     * @param controller
     * @param message
     * @throws PortException
     */
    @Throws(PortException::class)
    fun send(controller: PortController?, message: String) {
        if (this.controller == null || controller === this.controller) {
            send(message)
        } else {
            throw PortException("Port locked by another controller")
        }
    }

    /**
     * Release hold of this portHandler from given controller.
     *
     * @param controller
     * @throws PortLockException in case given holder is not the one that holds
     * handler
     */
    @Synchronized
    @Throws(PortLockException::class)
    fun releaseBy(controller: PortController?) {
        if (isLocked) {
            assert(controller != null)
            if (controller == this.controller) {
                this.controller = null
                execute {
                    portLock.unlock()
                    logger.debug("Unlocked by {}", controller)
                }
            } else {
                throw PortLockException("Can't unlock port with wrong controller")
            }
        } else {
            logger.warn("Attempting to release unlocked port")
        }
    }


    @Throws(Exception::class)
    override fun close() {
        executor.shutdown()
    }

    class PortTimeoutException(private val timeout: Duration) : PortException() {
        override val message: String = String.format("The timeout time of '%s' is exceeded", timeout)
    }

    override fun toMeta(): Meta {
        return meta
    }

    companion object {
        /**
         * The definition of default phrase condition
         *
         * @return
         */
        private val DEFAULT_PHRASE_CONDITION = { str: String -> str.endsWith("\n") }
    }
}
