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

package hep.dataforge.io.messages


import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * A coroutine based message server. It can dispatch incoming messages in a parallel way and organize single time pipelines for responders.
 * In theory, it could have any number of receivers.
 */
class MessageServer(val serverID: String) : Receiver {

    /**
     * Incoming message queue
     */
    private val queue: Channel<Envelope> = Channel(Channel.UNLIMITED)
    private var job: Job? = null

    private val targets: MutableMap<String, Receiver> = ConcurrentHashMap()

    /**
     * A dump for unclaimed messages
     */
    private var unclaimed: Receiver = object : Receiver {
        override fun receive(message: Envelope) {
            LoggerFactory.getLogger(javaClass).error("An envelope is unclaimed!")
            //TODO add debug info here
        }
    }

    override fun receive(message: Envelope) {
        async {
            queue.send(message)
        }
    }

    fun start() {
        job = launch {
            while (true) {
                val message = queue.receive()
                val target = getTarget(message)
                targets.getOrDefault(target, unclaimed).receive(message)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    private fun getTarget(message: Envelope): String {
        return message.target
    }

    fun addTarget(target: String, action: (Envelope) -> Unit){
        this.targets[target] = object : Receiver {
            override fun receive(message: Envelope) {
                action(message)
            }
        }
    }

    fun addTarget(target: String, receiver: Receiver){
        this.targets[target] = receiver
    }

    /**
     * Create a target which is removed after it receives a single message
     */
    private fun createTemporaryTarget(target: String, action: (Envelope) -> Unit) {
        val receiver = object : Receiver {
            override fun receive(message: Envelope) {
                action(message)
                targets.remove(target)
            }
        }
        this.targets[target] = receiver
    }

    suspend fun respond(target: String, message: Envelope): Envelope {
        //rewrap message to include back address?
        val origin = "@$serverID.ticket_${UUID.randomUUID()}"
        //TODO message meta is copied here, maybe use Laminate instead for performance
        val request: Envelope = EnvelopeBuilder(message).apply {
            this.origin = origin
            this.target = target
        }


        val ticket = CompletableDeferred<Envelope>()
        //create temporary target
        createTemporaryTarget(origin) {
            ticket.complete(it)
        }
        //send request
        receive(request)
        //wait for the response
        return ticket.await()
    }

}