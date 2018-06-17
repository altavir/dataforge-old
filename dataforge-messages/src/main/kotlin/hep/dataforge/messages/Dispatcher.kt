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


import hep.dataforge.io.envelopes.Envelope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory

/**
 * An object that receives a message and redirects it according to its target
 *
 *
 * @author Alexander Nozik
 */
interface Dispatcher: Receiver {
    fun dispatch(target: Target, message: Envelope)

    @JvmDefault
    override fun send(message: Message) {
        //TODO check for origin and target existence
        dispatch(message.target, message)
    }
}


/**
 * A coroutine based message server. It can dispatch incoming messages in a parallel way and organize single time pipelines for responders.
 * In theory, it could have any number of receivers.
 */
class BasicDispatcher(val targets: Map<Target, Receiver>) : Dispatcher, AutoCloseable {

    /**
     * Incoming message queue
     */
    private val queue: Channel<Pair<Target,Message>> = Channel(Channel.UNLIMITED)
    private var job: Job? = null

    /**
     * A dump for unclaimed messages
     */
    private var unclaimed: Receiver = object : Receiver {
        override fun send(message: Message) {
            LoggerFactory.getLogger(javaClass).error("A message is unclaimed!")
            //TODO add debug info here
        }
    }

    override fun close() {
        job?.cancel()
        job = null
    }

    private fun startJob(){
        if(job == null){
            job = launch {
                while (true) {
                    val received = queue.receive()
                    targets.getOrDefault(received.first, unclaimed).send(received.second)
                }
            }
        }
    }

    override fun dispatch(target: Target, message: Envelope) {
        startJob()
        async {
            queue.send(Pair(target,message))
        }
    }
}