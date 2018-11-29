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
import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.net.Socket

/**
 * @author Alexander Nozik
 */
class TcpPort(meta: Meta) : Port(meta) {

    constructor(ip: String, port: Int) : this(
            buildMeta("handler",
                    "type" to "tcp",
                    "ip" to ip,
                    "port" to port
            )
    )

    private var _socket: Socket? = null
    private val socket: Socket
        get() {
            if (_socket == null || _socket!!.isClosed) {
                _socket = Socket(getString("ip"), getInt("port"))
            }
            return _socket!!
        }

    private var listenerThread: Thread? = null

    @Volatile
    private var stopFlag = false

    override val isOpen: Boolean
        get() = listenerThread != null

    override val name: String
        get() = String.format("tcp::%s:%s", getString("ip"), getString("port"))

    @Throws(PortException::class)
    override fun open() {
        try {
            if (listenerThread == null) {
                stopFlag = false
                listenerThread = startListenerThread()
            }
        } catch (ex: IOException) {
            throw PortException(ex)
        }

    }

    //    @Override
    //    public void holdBy(PortController controller) throws PortException {
    //        super.holdBy(controller); //To change body of generated methods, choose Tools | Templates.
    //
    //        open();
    //
    //    }
    @Synchronized
    @Throws(Exception::class)
    override fun close() {
        if (_socket != null) {
            try {
                stopFlag = true
                listenerThread?.join(1500)
            } catch (ex: InterruptedException) {
                throw PortException(ex)
            } finally {
                listenerThread = null
                try {
                    socket.close()
                    _socket = null
                } catch (e: IOException) {
                    LoggerFactory.getLogger(javaClass).error("Failed to close socket", e)
                }

            }
        }
        super.close()
    }

    @Throws(IOException::class)
    private fun startListenerThread(): Thread {
        val task = {
            var reader: BufferedInputStream? = null
            while (!stopFlag) {
                try {
                    if (reader == null) {
                        reader = BufferedInputStream(socket.getInputStream())
                    }
                    //TODO switch to nio
                    receive(reader.read().toByte())
                } catch (ex: IOException) {
                    if (!stopFlag) {
                        LoggerFactory.getLogger(javaClass).error("TCP connection broken on {}. Reconnecting.", toString())
                        try {
                            _socket?.let {
                                it.close()
                                _socket = null
                            }
                            reader = BufferedInputStream(socket.getInputStream())
                        } catch (ex1: Exception) {
                            throw RuntimeException("Failed to reconnect tcp port")
                        }

                    } else {
                        LoggerFactory.getLogger(javaClass).info("Port listener stopped")
                    }
                }

            }

        }
        val thread = Thread(task, "port::" + toString() + "[listener]")
        thread.start()

        return thread
    }

    @Throws(PortException::class)
    public override fun send(message: String) {
        execute {
            try {
                val stream = socket.getOutputStream()
                stream.write(message.toByteArray())
                stream.flush()
                LoggerFactory.getLogger(javaClass).debug("SEND: $message")
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
        }
    }

}
