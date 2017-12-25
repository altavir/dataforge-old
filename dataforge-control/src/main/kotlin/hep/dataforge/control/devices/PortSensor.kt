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
import hep.dataforge.control.devices.PortSensor.Companion.PORT_STATE
import hep.dataforge.control.ports.GenericPortController
import hep.dataforge.control.ports.Port
import hep.dataforge.control.ports.PortFactory
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.events.EventBuilder
import hep.dataforge.exceptions.ControlException
import hep.dataforge.meta.Meta
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType.BOOLEAN
import hep.dataforge.values.ValueType.NUMBER
import java.time.Duration
import java.util.function.Predicate

/**
 * A Sensor that uses a Port to obtain data
 *
 * @param <T>
 * @author darksnake
 */
@StateDefs(
        StateDef(value = ValueDef(name = CONNECTED_STATE, type = [BOOLEAN], def = "false", info = "The connection state for this device"), writable = true),
        StateDef(value = ValueDef(name = PORT_STATE, info = "The name of the port to which this device is connected"), writable = true)
)
@ValueDefs(
        ValueDef(name = "timeout", type = arrayOf(NUMBER), def = "400", info = "A timeout for port response in milliseconds")
)
abstract class PortSensor<T>(context: Context, meta: Meta) : Sensor(context, meta) {

    //private Port port;
    private val port by stringState(PORT_STATE)
    protected var connection: GenericPortController? = null

    //    protected final void setPort(Port port) {
    //        this.connection = new GenericPortController(port);
    //    }

    val isConnected by booleanState(CONNECTED_STATE)

    protected val timeout: Duration
        get() = Duration.ofMillis(meta.getInt("timeout", 400)!!.toLong())

    @Throws(ControlException::class)
    protected open fun buildPort(portName: String): Port {
        logger.info("Connecting to port {}", portName)
        return PortFactory.getPort(portName)
    }


    @Throws(ControlException::class)
    override fun computeState(stateName: String): Any {
        return if (CONNECTED_STATE == stateName) {
            connection != null && connection!!.port.isOpen
        } else {
            throw RuntimeException("Physical state with name $stateName not found")
        }
    }

    override fun computeMetaState(stateName: String?): Meta {
        throw RuntimeException("Physical metastate with name $stateName not found")
    }

    private fun disconnect(){
        connection?.close()
        connection = null
        updateLogicalState(CONNECTED_STATE, false)
    }

    private fun connect(){
        if(!isConnected) {
            this.connection = GenericPortController(context, buildPort(port)).apply {
                //Add debug listener
                if (meta.getBoolean("debugMode", false)) {
                    onPhrase { phrase -> logger.debug("Device {} received phrase: {}", name, phrase) }
                    onError { message, error -> logger.error("Device {} exception: {}", name, message, error) }
                }
                open()
            }
            updateLogicalState(CONNECTED_STATE, true)
        }
    }


    @Throws(ControlException::class)
    override fun shutdown() {
        try {
            connection!!.close()
            //PENDING do we need not to close the port sometimes. Should it be configurable?
            connection!!.port.close()
            updateLogicalState(CONNECTED_STATE, false)
        } catch (ex: Exception) {
            throw ControlException(ex)
        }

        super.shutdown()
    }

    protected fun sendAndWait(request: String): String {
        return connection!!.sendAndWait(request, timeout) { it -> true }
    }

    protected fun sendAndWait(request: String, predicate: Predicate<String>): String {
        return connection!!.sendAndWait(request, timeout, predicate)
    }

    protected fun send(message: String) {
        if(connection == null){
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

    @Throws(ControlException::class)
    override fun requestStateChange(stateName: String, value: Value) {
        if (stateName == CONNECTED_STATE) {
            if (value.booleanValue()) {
                connection!!.open()
                updateLogicalState(CONNECTED_STATE, true)
            } else {
                try {
                    connection!!.close()
                    updateLogicalState(CONNECTED_STATE, false)
                } catch (e: Exception) {
                    throw ControlException("Failed to close the connection", e)
                }

            }
        }
    }

    companion object {

        const val CONNECTED_STATE = "connected"
        const val PORT_STATE = "port"
    }

    /*
     * @return the port
     * @throws hep.dataforge.exceptions.ControlException
     */
    //    protected Port getPort() throws ControlException {
    //        if (port == null) {
    //            String port = meta().getString(PORT_STATE);
    //            setPort(buildPort(port));
    //            this.port.open();
    //            updateLogicalState(CONNECTED_STATE, true);
    //        }
    //        return port;
    //    }

}
