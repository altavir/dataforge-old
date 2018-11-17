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
import hep.dataforge.io.envelopes.Envelope

/**
 * An object that receives a message and redirects it according to its target
 *
 *
 * @author Alexander Nozik
 */
interface Dispatcher : Receiver {

    /**
     * Dispatch given message to given target. Evaluated asynchronously.
     * Target overrides target in message if needed.
     */
    fun dispatch(target: Target, message: Message)

    /**
     * Infer the target from the message itself
     */
    @JvmDefault
    override fun send(message: Message) {
        //TODO check for origin and target existence
        dispatch(message.target, message)
    }
}


/**
 * A coroutine based message server. It can dispatch incoming messages in a parallel way and organize single time pipelines for responders.
 * In theory, it could have any number of receivers.
 * @param context a context to run dispatcher in
 * @param fixed map of targets
 * @param unclaimedAction action to be performed on unclaimed messages
 */
class BasicDispatcher(
        override val context: Context,
        private val targets: Map<Target, Receiver>,
        unclaimedAction: (suspend (Message) -> Unit)? = null
) : Dispatcher, AutoCloseable, ContextAware {

    /**
     * Incoming message queue
     */
    private val queue: Channel<Pair<Target, Message>> = Channel(Channel.UNLIMITED)
    private val unclaimedAction: suspend (Message) -> Unit = unclaimedAction
            ?: { logger.error("A message in dispatcher $this is unclaimed!") }
    private var job: Job? = null

    override fun close() {
        job?.cancel()
        job = null
    }

    private fun startJob() {
        if (job == null) {
            job = launch(context.scope) {
                while (true) {
                    val received = queue.receive()
                    //Resend message or dump it if receiver not found
                    targets[received.first]?.send(received.second) ?: launch { unclaimedAction(received.second) }
                }
            }
        }
    }

    override fun dispatch(target: Target, message: Envelope) {
        startJob()
        launch(context.scope) {
            queue.send(Pair(target, message))
        }
    }
}