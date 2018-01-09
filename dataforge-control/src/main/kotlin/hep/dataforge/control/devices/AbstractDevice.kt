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
package hep.dataforge.control.devices

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.control.Connection
import hep.dataforge.control.ConnectionHelper
import hep.dataforge.events.Event
import hep.dataforge.events.EventHandler
import hep.dataforge.exceptions.ControlException
import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.meta.Meta
import hep.dataforge.names.AnonymousNotAlowed
import hep.dataforge.utils.MetaHolder
import hep.dataforge.utils.Optionals
import hep.dataforge.values.Value
import java.time.Duration
import java.util.*
import java.util.concurrent.*

/**
 *
 *
 * State has two components: physical and logical. If logical state does not
 * coincide with physical, it should be invalidated and automatically updated on
 * next request.
 *
 *
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
abstract class AbstractDevice(private val context: Context?, meta: Meta) : MetaHolder(meta), Device {

    private val states = HashMap<String, Value>()
    private val metastates = HashMap<String, Meta>()
    private var connectionHelper: ConnectionHelper? = null
    /**
     * A single thread executor for this device. All state changes and similar work must be done on this thread.
     *
     * @return
     */
    protected val executor = Executors.newSingleThreadScheduledExecutor { r ->
        val res = Thread(r)
        res.name = "device::" + name
        res.priority = Thread.MAX_PRIORITY
        res.isDaemon = true
        res
    }

    init {
        //initialize states
        stateDefs.stream()
                .filter { !it.value.def.isEmpty() }
                .forEach { states.put(it.value.name, Value.of(it.value.def)) }
    }

    override fun getConnectionHelper(): ConnectionHelper {
        if (connectionHelper == null) {
            connectionHelper = ConnectionHelper(this, this.logger)
        }
        return connectionHelper!!
    }


    @Throws(ControlException::class)
    override fun init() {
        logger.info("Initializing device '{}'...", name)
        updateLogicalState(Device.INITIALIZED_STATE, true)
    }

    @Throws(ControlException::class)
    override fun shutdown() {
        logger.info("Shutting down device '{}'...", name)
        forEachConnection(Connection::class.java) { c ->
            try {
                c.close()
            } catch (e: Exception) {
                logger.error("Failed to close connection", e)
            }
        }
        updateLogicalState(Device.INITIALIZED_STATE, false)
        executor.shutdown()
    }


    override fun getContext(): Context {
        if (context == null) {
            logger.warn("Context for device not defined. Using GLOBAL context.")
            return Global.instance()
        } else {
            return this.context
        }
    }

    override fun getName(): String {
        return meta.getString("name", type)
    }

    protected fun execute(runnable: ()->Unit): Future<*> {
        return executor.submit(runnable)
    }

    protected fun <T> call(callable: () -> T): Future<T> {
        return executor.submit(callable)
    }

    protected fun schedule(delay: Duration, runnable: Runnable): ScheduledFuture<*> {
        return executor.schedule(runnable, delay.toMillis(), TimeUnit.MILLISECONDS)
    }

    /**
     * Update logical state if it is changed
     *
     * @param stateName
     * @param stateValue
     */
    protected fun updateLogicalState(stateName: String, stateValue: Any) {
        if (stateValue is Meta) {
            updateLogicalMetaState(stateName, stateValue)
        } else {
            val oldState = this.states[stateName]
            val newState = Value.of(stateValue)
            //Notify only if state really changed
            if (newState != oldState) {
                //Update logical state and notify listeners.
                execute {
                    this.states.put(stateName, newState)
                    if (newState.isNull) {
                        logger.info("State {} is reset", stateName)
                    } else {
                        logger.info("State {} changed to {}", stateName, newState)
                    }
                    forEachConnection(DeviceListener::class.java) {
                        it.notifyDeviceStateChanged(this, stateName, newState)
                    }
                }
            }
        }
    }

    protected fun updateLogicalMetaState(stateName: String, metaStateValue: Meta) {
        val oldState = this.metastates[stateName]
        //Notify only if state really changed
        if (metaStateValue != oldState) {
            //Update logical state and notify listeners.
            execute {
                this.metastates.put(stateName, metaStateValue)
                if (metaStateValue.isEmpty) {
                    logger.info("Metastate {} is reset", stateName)
                } else {
                    logger.info("Metastate {} changed to {}", stateName, metaStateValue)
                }
                forEachConnection(DeviceListener::class.java) {
                    it.notifyDeviceStateChanged(this, stateName, metaStateValue)
                }
            }
        }
    }

    protected fun notifyError(message: String, error: Throwable) {
        logger.error(message, error)
        forEachConnection(DeviceListener::class.java) {
            it.evaluateDeviceException(this, message, error)
        }
    }

    protected fun dispatchEvent(event: Event) {
        forEachConnection(EventHandler::class.java) { it -> it.pushEvent(event) }
    }

    /**
     * Reset state to its default value if it is present
     */
    fun resetState(stateName: String) {
        run {
            this.states.remove(stateName)
            stateDefs.stream()
                    .filter { it.value.name == stateName }
                    .findFirst()
                    .ifPresent { value -> states.put(stateName, Value.of(value)) }
        }
    }

    /**
     * Get logical state
     *
     * @param stateName
     * @return
     */
    protected fun getLogicalState(stateName: String): Value {
        return Optionals.either(Optional.ofNullable(states[stateName]))
                .or(optStateDef(stateName).map<String> { it.value.def }.map { Value.of(it) })
                .opt()
                .orElseThrow { RuntimeException("Can't calculate state " + stateName) }
    }

    /**
     * Request the change of physical and/or logical state.
     *
     * @param stateName
     * @param value
     * @throws ControlException
     */
    @Throws(ControlException::class)
    protected open fun requestStateChange(stateName: String, value: Value) {
        if (stateName == Device.INITIALIZED_STATE) {
            if (value.booleanValue()) {
                init()
            } else {
                shutdown()
            }
        } else {
            throw NameNotFoundException("State with given name not found", stateName)
        }
    }

    /**
     * Request the change of physical ano/or logical meta state.
     *
     * @param stateName
     * @param meta
     * @throws ControlException
     */
    @Throws(ControlException::class)
    protected open fun requestMetaStateChange(stateName: String, meta: Meta) {
        throw NameNotFoundException("Meta state with given name not found", stateName)
    }


    /**
     * Compute physical state
     *
     * @param stateName
     * @return
     * @throws ControlException
     */
    open fun computeState(stateName: String): Any {
        throw RuntimeException("Physical state with name $stateName not found")
    }

    /**
     * Compute physical meta state
     *
     * @param stateName
     * @return
     * @throws ControlException
     */
    open fun computeMetaState(stateName: String): Meta {
        throw RuntimeException("Physical metastate with name $stateName not found")
    }

    /**
     * Request state change and update result
     *
     * @param stateName
     * @param value
     */
    override fun setState(stateName: String, value: Any) {
        run {
            try {
                requestStateChange(stateName, Value.of(value))
            } catch (e: Exception) {
                logger.error("Failed to set state {} to {} with exception: {}", stateName, value, e.toString())
            }
        }
    }

    override fun setMetaState(stateName: String, meta: Meta) {
        run {
            try {
                requestMetaStateChange(stateName, meta)
            } catch (e: Exception) {
                logger.error("Failed to set  metastate {} to {} with exception: {}", stateName, meta, e.toString())
            }
        }
    }

    fun getStateInFuture(stateName: String): Future<Value> {
        return call {
            states.computeIfAbsent(stateName) { t: String ->
                try {
                    states.computeIfAbsent(t) { Value.of(computeState(it)) }
                } catch (ex: ControlException) {
                    notifyError("Can't calculate state " + stateName, ex)
                    states.computeIfAbsent(t) { Value.NULL }
                }
            }
        }
    }

    override fun getState(stateName: String): Value {
        try {
            return getStateInFuture(stateName).get()
        } catch (e: InterruptedException) {
            throw RuntimeException("Failed to calculate state " + stateName)
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to calculate state " + stateName)
        }

    }

    override fun optState(stateName: String): Optional<Value> {
        return if (states.containsKey(stateName)) {
            Optional.ofNullable(states[stateName])
        } else {
            super.optState(stateName)
        }
    }

    fun getMetaStateInFuture(stateName: String): Future<Meta> {
        return call {
            this.metastates.computeIfAbsent(stateName) {
                metastates.computeIfAbsent(stateName) { state ->
                    try {
                        metastates.computeIfAbsent(state) { computeMetaState(stateName) }
                    } catch (ex: ControlException) {
                        notifyError("Can't calculate metastate " + stateName, ex)
                        metastates.computeIfAbsent(state) { Meta.empty() }
                    }
                }
            }
        }
    }

    override fun optMetaState(stateName: String): Optional<Meta> {
        return if (states.containsKey(stateName)) {
            Optional.ofNullable(metastates[stateName])
        } else {
            super.optMetaState(stateName)
        }
    }

    override fun getMetaState(stateName: String): Meta {
        try {
            return getMetaStateInFuture(stateName).get()
        } catch (e: InterruptedException) {
            throw RuntimeException("Failed to calculate metastate " + stateName)
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to calculate metastate " + stateName)
        }

    }

    override fun getType(): String {
        return meta.getString("type", "unknown")
    }
}
