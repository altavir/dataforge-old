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

package hep.dataforge.messages

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.coroutineContext
import hep.dataforge.io.envelopes.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import java.net.Socket

/**
 * A two directional client socket for messages
 */
class MessageSocket(override val context: Context, private val receiver: Receiver, private val socketFactory: () -> Socket) : Receiver, ContextAware, AutoCloseable {
    private var socket: Socket = socketFactory()
        get() {
            synchronized(this) {
                if (field.isClosed) {
                    logger.info("Socket is closed, creating new socket.")
                    field = socketFactory()
                }
                return field
            }
        }

    // A buffer for incoming messages
    private val incoming: Channel<Envelope> = Channel(Channel.UNLIMITED)
    // A buffer for outgoing messages
    private val outgoing: Channel<Envelope> = Channel(Channel.UNLIMITED)

    private var parentJob: Job? = null


    fun open() {
        val job = Job()
        parentJob = job
        launch(context.coroutineContext, parent = job) {
            val stream = socket.getOutputStream()
            val writer = DefaultEnvelopeWriter(DefaultEnvelopeType.INSTANCE, binaryMetaType)

            while (true) {
                val envelope = outgoing.receive()
                writer.write(stream, envelope)
            }
        }

        launch(context.coroutineContext, parent = job) {
            val stream = socket.getInputStream()
            val reader = DefaultEnvelopeReader.INSTANCE

            while (true) {
                incoming.send(reader.read(stream))
            }
        }

        launch(context.coroutineContext, parent = job) {
            while (true) {
                receiver.send(incoming.receive())
            }
        }
    }

    override fun close() {
        parentJob?.cancel()
        socket.close()
    }

    /**
     * send message via socket
     */
    override fun send(message: Envelope) {
        launch(context.coroutineContext) {
            incoming.send(message)
        }
    }
}