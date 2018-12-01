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

import hep.dataforge.context.Global
import hep.dataforge.exceptions.PortException
import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

/**
 * @author Alexander Nozik
 */
class TcpPort(val ip: String, val port: Int, val config: Meta = Meta.empty()) : Port() {

    private var channel: SocketChannel = SocketChannel.open()

    override val isOpen: Boolean
        get() = channel.isConnected

    override val name = String.format("tcp::%s:%d", ip, port)

    private val dispatcher = this.executor.asCoroutineDispatcher()

    private var listenerJob: Job? = null

    private fun openChannel(): SocketChannel{
        return SocketChannel.open(InetSocketAddress(ip, port)).apply {
            this.configureBlocking(false)
        }
    }

    @Throws(PortException::class)
    override fun open() {
        execute {
            if (!channel.isConnected && !channel.isConnectionPending) {
                channel = openChannel()
                channel.finishConnect()
                startListener()
            }
        }
    }

    @Synchronized
    @Throws(Exception::class)
    override fun close() {
        execute {
            if(isOpen) {
                listenerJob?.cancel()
                channel.shutdownInput()
                channel.shutdownOutput()
                channel.close()
                super.close()
            }
        }
    }

    private fun startListener() {
        listenerJob = Global.launch(dispatcher) {
            val buffer = ByteBuffer.allocate(1024)
            while (true) {
                try {
                    //read all content
                    do {
                        val num = channel.read(buffer)
                        buffer.flip()
                        if (num > 0) {
                            receive(buffer.array())
                        }
                        buffer.rewind()
                    } while (num > 0)
                    delay(50)
                } catch (ex: Exception) {
                    logger.error("Channel read error", ex)
                    logger.info("Reconnecting")
                    channel = openChannel()
                }
            }
        }
    }

    @Throws(PortException::class)
    public override fun send(message: ByteArray) {
        execute {
            try {
                channel.write(ByteBuffer.wrap(message))
                logger.debug("SEND: $message")
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }
    }

    override fun toMeta(): Meta = buildMeta {
        "type" to "tcp"
        "name" to this@TcpPort.name
        "ip" to ip
        "port" to port
    }
}
