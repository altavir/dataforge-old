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

import hep.dataforge.io.envelopes.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import java.net.Socket

/**
 * A two directional client socket for messages
 */
class MessageSocket(val receiver: Receiver, private val socketFactory: () -> Socket) : Receiver {
    private var socket: Socket = socketFactory()
        get() {
            if (field.isClosed) {
                field = socketFactory()
            }
            return field
        }

    // A buffer for incoming messages
    private val incoming: Channel<Envelope> = Channel(Channel.UNLIMITED)
    // A buffer for outgoing messages
    private val outgoing: Channel<Envelope> = Channel(Channel.UNLIMITED)
    private var sendJob: Job? = null
    private var receiveJob: Job? = null
    private var relayJob: Job? = null


    fun start() {
        sendJob = launch {
            val stream = socket.getOutputStream()
            val writer = DefaultEnvelopeWriter(DefaultEnvelopeType.INSTANCE, binaryMetaType)

            while (true) {
                val envelope = outgoing.receive()
                writer.write(stream, envelope)
            }
        }

        receiveJob = launch {
            val stream = socket.getInputStream()
            val reader = DefaultEnvelopeReader.INSTANCE

            while (true) {
                incoming.send(reader.read(stream))
            }
        }

        relayJob = launch {
            while (true) {
                receiver.receive(incoming.receive())
            }
        }
    }

    fun stop() {
        sendJob?.cancel()
        receiveJob?.cancel()
        relayJob?.cancel()
        socket.close()
    }

    /**
     * send message via socket
     */
    override fun receive(message: Envelope) {
        async {
            outgoing.send(message)
        }
    }
}