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

import hep.dataforge.exceptions.EnvelopeTargetNotFoundException
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import java.util.concurrent.CompletableFuture

/**
 * An interface marking some object that can respond to envelopes.
 * @author Alexander Nozik
 */
interface Responder {
    /**
     * A synchronous respond for the message
     * @param message
     * @return
     */
    fun respond(message: Envelope): Envelope

    /**
     * Asynchronous wrapper for response operation
     * @param message
     * @return
     */
    fun respondInFuture(message: Envelope): CompletableFuture<Envelope> {
        return CompletableFuture.supplyAsync { respond(message) }
    }
}

/**
 * A dispatcher of messages that could provide appropriate responder for
 * message. The dispatcher does not handle message itself
 *
 * @author Alexander Nozik
 */
interface Dispatcher {

    @Throws(EnvelopeTargetNotFoundException::class)
    fun getResponder(targetInfo: Meta): Responder

    fun getResponder(envelope: Envelope): Responder {
        return getResponder(envelope.meta.getMeta(MESSAGE_TARGET_NODE))
    }

    companion object {
        const val MESSAGE_TARGET_NODE = "@message.target"
        const val TARGET_TYPE_KEY = "type"
        const val TARGET_NAME_KEY = "name"
    }
}

/**
 * An object that can receive an envelope without a response
 */
interface Receiver {
    //TODO add meta status as a response?
    fun receive(message: Envelope)
}

/**
 * A validator checking incoming messages. It colud be used for security or bug checks
 * Created by darksnake on 12-Oct-16.
 */
interface Validator {

    fun validate(message: Envelope): Meta

    fun isValid(message: Envelope): Boolean {
        return validate(message).getBoolean(IS_VALID_KEY)
    }

    companion object {
        const val IS_VALID_KEY = "isValid"
        const val MESSAGE_KEY = "message"

        fun valid(): Meta {
            return MetaBuilder("validationResult").putValue(IS_VALID_KEY, true).build()
        }

        fun invalid(vararg message: String): Meta {
            return MetaBuilder("validationResult")
                    .putValue(IS_VALID_KEY, false)
                    .putValue(MESSAGE_KEY, message)
                    .build()
        }
    }
}

val Envelope.target: String
    get() = this.meta.getString("@message.target")

var EnvelopeBuilder.target: String
    get() = this.meta.getString("@message.target")
    set(value) {
        this.meta.setValue("@message.target", value)
    }

val Envelope.origin: String
    get() = this.meta.getString("@message.origin")

var EnvelopeBuilder.origin: String
    get() = this.meta.getString("@message.origin")
    set(value) {
        this.meta.setValue("@message.origin", value)
    }