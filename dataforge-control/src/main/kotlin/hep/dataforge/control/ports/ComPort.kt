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

import hep.dataforge.exceptions.PortException
import hep.dataforge.kodex.buildMeta
import hep.dataforge.meta.Meta
import jssc.SerialPort
import jssc.SerialPort.*
import jssc.SerialPortEventListener
import jssc.SerialPortException
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * @author Alexander Nozik
 */
class ComPort(meta: Meta) : Port(meta) {

    //    private static final int CHAR_SIZE = 1;
    //    private static final int MAX_SIZE = 50;
    private var port: SerialPort? = null

    private val serialPortListener = SerialPortEventListener { event ->
        if (event.isRXCHAR) {
            val chars = event.eventValue
            try {
                val bytes = port!!.readBytes(chars)
                receive(bytes)
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            } catch (ex: SerialPortException) {
                throw RuntimeException(ex)
            }
        }
    }

    override val isOpen: Boolean
        get() = port?.isOpened ?: false

    override val name: String
        get() = String.format("com::%s", getString("name"))

    override fun toString(): String {
        return name
    }

    @Throws(PortException::class)
    override fun open() {
        try {
            if (port == null) {
                port = SerialPort(getString("name")).apply {
                    openPort()
                    val an = meta
                    val baudRate = an.getInt("baudRate", BAUDRATE_9600)
                    val dataBits = an.getInt("dataBits", DATABITS_8)
                    val stopBits = an.getInt("stopBits", STOPBITS_1)
                    val parity = an.getInt("parity", PARITY_NONE)
                    setParams(baudRate, dataBits, stopBits, parity)
                    addEventListener(serialPortListener)
                }
            }
        } catch (ex: SerialPortException) {
            throw PortException("Can't open the port", ex)
        }

    }

    @Throws(PortException::class)
    fun clearPort() {
        try {
            port?.purgePort(PURGE_RXCLEAR or PURGE_TXCLEAR)
        } catch (ex: SerialPortException) {
            throw PortException(ex)
        }

    }

    @Throws(Exception::class)
    override fun close() {
        port?.let {
            it.removeEventListener()
            if (it.isOpened) {
                it.closePort()
            }
        }
        port = null
        super.close()
    }

    @Throws(PortException::class)
    public override fun send(message: String) {
        if (!isOpen) {
            open()
        }
        execute {
            try {
                LoggerFactory.getLogger(javaClass).debug("SEND: $message")
                port!!.writeString(message)
            } catch (ex: SerialPortException) {
                throw RuntimeException(ex)
            }
        }
    }

    companion object {

        /**
         * Construct ComPort with default parameters:
         *
         *
         * Baud rate: 9600
         *
         *
         * Data bits: 8
         *
         *
         * Stop bits: 1
         *
         *
         * Parity: non
         *
         * @param portName
         */
        @JvmOverloads
        fun create(portName: String, baudRate: Int = BAUDRATE_9600, dataBits: Int = DATABITS_8, stopBits: Int = STOPBITS_1, parity: Int = PARITY_NONE): ComPort {
            return ComPort(buildMeta {
                setValue("type", "com")
                putValue("name", portName)
                putValue("baudRate", baudRate)
                putValue("dataBits", dataBits)
                putValue("stopBits", stopBits)
                putValue("parity", parity)
            })
        }
    }
}

