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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices

import hep.dataforge.context.Context
import hep.dataforge.control.devices.PortSensor.Companion.CONNECTED_STATE
import hep.dataforge.control.devices.PortSensor.Companion.DEBUG_STATE
import hep.dataforge.control.devices.PortSensor.Companion.PORT_STATE
import hep.dataforge.control.ports.GenericPortController
import hep.dataforge.control.ports.PortFactory
import hep.dataforge.description.NodeDef
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.events.EventBuilder
import hep.dataforge.exceptions.ControlException
import hep.dataforge.kodex.useMeta
import hep.dataforge.kodex.useValue
import hep.dataforge.meta.Meta
import hep.dataforge.states.MetaStateDef
import hep.dataforge.states.StateDef
import hep.dataforge.states.StateDefs
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType.BOOLEAN
import hep.dataforge.values.ValueType.NUMBER
import java.time.Duration

/**
 * A Sensor that uses a Port to obtain data
 *
 * @param <T>
 * @author darksnake
 */
@StateDefs(
        StateDef(value = ValueDef(name = CONNECTED_STATE, type = [BOOLEAN], def = "false", info = "The connection state for this device"), writable = true),
        StateDef(value = ValueDef(name = PORT_STATE, info = "The name of the port to which this device is connected")),
        StateDef(value = ValueDef(name = DEBUG_STATE, type = [BOOLEAN], def = "false", info = "If true, then all received phrases would be shown in the log"), writable = true)
)
@MetaStateDef(value = NodeDef(name = "port", from = "method::hep.dataforge.control.ports.PortFactory.build", info = "Information about port"), writable = true)
@ValueDefs(
        ValueDef(name = "timeout", type = arrayOf(NUMBER), def = "400", info = "A timeout for port response in milliseconds")
)
abstract class PortSensor<T>(context: Context, meta: Meta) : Sensor(context, meta) {

    protected var connection: GenericPortController? = null

    var connected by booleanState(CONNECTED_STATE)
    var debug by booleanState(DEBUG_STATE)

    private val defaultTimeout: Duration = Duration.ofMillis(meta.getInt("timeout", 400).toLong())

    init {
        meta.useMeta(PORT_STATE) {
            setMetaState(PORT_STATE, it)
        }
        meta.useValue(DEBUG_STATE) {
            setState(DEBUG_STATE, it)
        }
    }

    @Throws(ControlException::class)
    override fun computeState(stateName: String): Any {
        return if (CONNECTED_STATE == stateName) {
            connection?.port?.isOpen ?: false
        } else {
            super.computeState(stateName)
        }
    }

    private fun setDebugMode(debugMode: Boolean) {
        //Add debug listener
        if (debugMode) {
            connection?.apply {
                onAnyPhrase("$name[debug]") { phrase -> logger.debug("Device {} received phrase: {}", name, phrase) }
                onError("$name[debug]") { message, error -> logger.error("Device {} exception: {}", name, message, error) }
            }
        } else {
            connection?.apply {
                removePhraseListener("$name[debug]")
                removeErrorListener("$name[debug]")
            }
        }
        updateLogicalState(DEBUG_STATE, debugMode)
    }

    @Throws(ControlException::class)
    override fun requestStateChange(stateName: String, value: Value) {
        when (stateName) {
            CONNECTED_STATE -> if (value.booleanValue()) {
                connection?.open() ?: throw ControlException("Not connected to port")
                updateLogicalState(CONNECTED_STATE, true)
            } else {
                connection?.close()
                connection = null
                updateLogicalState(CONNECTED_STATE, false)
            }
            DEBUG_STATE -> setDebugMode(value.booleanValue())
            else -> super.requestStateChange(stateName, value)
        }
    }

    protected open fun connect(meta: Meta): GenericPortController {
        val port = PortFactory.build(meta)
        return GenericPortController(context, port)
    }

    private fun setupConnection(portMeta: Meta) {
        connection?.close()
        this.connection = connect(portMeta)
        if (connected) {
            connection?.open()
        }
        setDebugMode(debug)
        updateLogicalMetaState(PORT_STATE, portMeta)
        updateLogicalState(PORT_STATE, portMeta)
    }

    override fun requestMetaStateChange(stateName: String, meta: Meta) {
        if (stateName == PORT_STATE) {
            setupConnection(meta)
        } else {
            super.requestMetaStateChange(stateName, meta)
        }
    }

    @Throws(ControlException::class)
    override fun shutdown() {
        setState(CONNECTED_STATE, false)
//        connection?.port?.close()
        super.shutdown()
    }

    protected fun sendAndWait(request: String, timeout: Duration = defaultTimeout): String {
        return connection?.sendAndWait(request, timeout) { true } ?: throw ControlException("Not connected to port")
    }

    protected fun sendAndWait(request: String, timeout: Duration = defaultTimeout, predicate: (String) -> Boolean): String {
        return connection?.sendAndWait(request, timeout, predicate) ?: throw ControlException("Not connected to port")
    }

    protected fun send(message: String) {
        if (connection == null) {
            throw ControlException("Not connected to port")
        }
        connection?.send(message)
        dispatchEvent(
                EventBuilder
                        .make(name)
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
