/*
 * Copyright  2018 Alexander Nozik.
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

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.control.devices.Device
import hep.dataforge.control.devices.dispatchEvent
import hep.dataforge.control.devices.notifyError
import hep.dataforge.events.EventBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.nullable
import hep.dataforge.states.StateHolder
import hep.dataforge.states.Stateful
import hep.dataforge.states.metaState
import hep.dataforge.states.valueState
import hep.dataforge.useValue
import org.slf4j.Logger
import java.time.Duration

class PortHelper(
        val device: Device,
        val builder: ((Context, Meta) -> GenericPortController) = { context, meta -> GenericPortController(context, PortFactory.build(meta)) }
) : Stateful, ContextAware {
    override val logger: Logger
        get() = device.logger

    override val states: StateHolder
        get() = device.states

    override val context: Context
        get() = device.context

    private var _connection: GenericPortController? = null
    val connection: GenericPortController
        get() = _connection ?: throw RuntimeException("Not connected")

    val connectedState = valueState(CONNECTED_STATE, getter = { connection.port.isOpen }) { old, value ->
        if (old != value) {
            logger.info("State 'connect' changed to $value")
            connect(value.boolean)
        }
        update(value)
    }

    var connected by connectedState.booleanDelegate

    var debug by valueState(DEBUG_STATE) { old, value ->
        if (old != value) {
            logger.info("Turning debug mode to $value")
            setDebugMode(value.boolean)
        }
        update(value)
    }.booleanDelegate

    var port by metaState(PORT_STATE, getter = { connection.port.meta }) { old, value ->
        if (old != value) {
            setupConnection(value)
        }
        update(value)
    }.delegate

    private val defaultTimeout: Duration = Duration.ofMillis(device.meta.getInt("port.timeout", 400).toLong())

    val name get() = device.name

    init {
        device.meta.useValue(DEBUG_STATE) {
            states.update(DEBUG_STATE, it.boolean)
        }
    }

    private fun setDebugMode(debugMode: Boolean) {
        //Add debug listener
        if (debugMode) {
            connection.apply {
                onAnyPhrase("$name[debug]") { phrase -> logger.debug("Device {} received phrase: \n{}", name, phrase) }
                onError("$name[debug]") { message, error -> logger.error("Device {} exception: \n{}", name, message, error) }
            }
        } else {
            connection.apply {
                removePhraseListener("$name[debug]")
                removeErrorListener("$name[debug]")
            }
        }
        states.update(DEBUG_STATE, debugMode)
    }

    private fun connect(connected: Boolean) {
        if (connected) {
            try {
                if (_connection == null) {
                    logger.debug("Setting up connection using device meta")
                    val portMeta: Meta = device.meta.optMeta(PORT_STATE).nullable
                            ?: device.meta.optValue(PORT_STATE).map {
                                PortFactory.nameToMeta(it.string)
                            }.orElse(Meta.empty())
                    setupConnection(portMeta)
                }
                connection.open()
                this.connectedState.update(true)
            } catch (ex: Exception) {
                device.notifyError("Failed to open connection", ex)
                this.connectedState.update(false)
            }
        } else {
            _connection?.close()
            _connection = null
            this.connectedState.update(false)
        }
    }

    private fun setupConnection(portMeta: Meta) {
        _connection?.close()
        this._connection = builder(device.context, portMeta)
        setDebugMode(debug)
        states.update(PORT_STATE, portMeta)
    }

    fun shutdown() {
        connectedState.set(false)
    }

    fun sendAndWait(request: String, timeout: Duration = defaultTimeout): String {
        return connection.sendAndWait(request, timeout) { true }
    }

    fun sendAndWait(request: String, timeout: Duration = defaultTimeout, predicate: (String) -> Boolean): String {
        return connection.sendAndWait(request, timeout, predicate)
    }

    fun send(message: String) {
        connected = true
        connection.send(message)
        device.dispatchEvent(
                EventBuilder
                        .make(device.name)
                        .setMetaValue("request", message)
                        .build()
        )
    }

    companion object {
        const val CONNECTED_STATE = "connected"
        const val PORT_STATE = "port"
        const val DEBUG_STATE = "debug"
    }

}