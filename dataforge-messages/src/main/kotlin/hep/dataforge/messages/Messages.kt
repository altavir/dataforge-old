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
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.names.Name
import hep.dataforge.nullable
import kotlinx.coroutines.Deferred

//Message operations
const val ACTION_KEY = "@message.action"
const val PUSH_ACTION = "push"
const val PULL_ACTION = "pull"

const val MESSAGE_STATUS_KEY = "@message.status"
const val MESSAGE_TERMINATE = "terminate"
const val MESSAGE_OK = "ok"
const val MESSAGE_FAIL = "error"

const val MESSAGE_TYPE_KEY = "@message.type"
const val RESPONSE_SUCCESS_KEY = "success"
const val RESPONSE_TYPE_SUFFIX = ".response"
const val ERROR_RESPONSE_TYPE = "error"

//TODO replace by inline classes?
typealias Message = Envelope

typealias MessageBuilder = EnvelopeBuilder
typealias Target = Meta

/**
 * The target of the message. Must exist for message to be valid
 */
val Message.target: Target
    get() = this.meta.getMeta("@message.target")

var MessageBuilder.target: Target
    get() = this.meta.getMeta("@message.target")
    set(value) {
        this.meta.setNode("@message.target", value)
    }

/**
 * The origin node of the message. Could be null in case of anonymous message.
 * Nodes could ignore anonymous messages.
 */
val Message.origin: Target?
    get() = this.meta.optMeta("@message.origin").nullable

var MessageBuilder.origin: Target?
    get() = this.meta.optMeta("@message.origin").nullable
    set(value) {
        this.meta.setNode("@message.origin", value)
    }

val Target.id: Name
    get() = Name.of(this.getString("name"))


/**
 * An object that can receive an envelope without a response
 */
interface Receiver {

    /**
     * Evaluate message. The receiver is responsible to launch message evaluation of the calling thread.
     */
    fun send(message: Message)
}

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
    suspend fun respond(message: Message): Message

    @JvmDefault
    fun respondInFuture(message: Message): Deferred<Message> {
        return async { respond(message) }
    }
}


/**
 * A validator checking incoming messages. It colud be used for security or bug checks
 * Created by darksnake on 12-Oct-16.
 */
interface Validator {

    /**
     * Validate message and return result as meta
     */
    fun validate(message: Message): Meta

    /**
     * Simplified validation result
     */
    fun isValid(message: Message): Boolean {
        return validate(message).getBoolean(IS_VALID_KEY)
    }

    companion object {
        const val IS_VALID_KEY = "isValid"
        const val MESSAGE_KEY = "message"

        /**
         * Simple valid result
         */
        val valid: Meta = MetaBuilder("validationResult").putValue(IS_VALID_KEY, true).build()


        /**
         * Simple invalid result with list of messages
         */
        fun invalid(vararg message: String): Meta {
            return MetaBuilder("validationResult")
                    .putValue(IS_VALID_KEY, false)
                    .putValue(MESSAGE_KEY, message)
                    .build()
        }
    }
}

/**
 * Generate a response base with the same meta parameters (type, encoding)
 * as in request and modified message type if it is present
 *
 * @param request
 * @return
 */
fun responseBase(request: Message): MessageBuilder {
    val res = EnvelopeBuilder()
    val type = request.meta.getString(MESSAGE_TYPE_KEY, "")
    if (!type.isEmpty()) {
        res.setMetaValue(MESSAGE_TYPE_KEY, type + RESPONSE_TYPE_SUFFIX)
    }
    return res
}

/**
 * Response base with given type (could be null) and default meta parameters
 *
 * @param type
 * @return
 */
fun responseBase(type: String?): MessageBuilder {
    var aType = type
    val res = EnvelopeBuilder()
    if (aType != null && !aType.isEmpty()) {
        if (!aType.endsWith(RESPONSE_TYPE_SUFFIX)) {
            aType += RESPONSE_TYPE_SUFFIX
        }
        res.setMetaValue(MESSAGE_TYPE_KEY, aType)
    }
    return res
}

/**
 * Request base with given type (could be null) and default meta parameters
 *
 * @param type
 * @return
 */
fun requestBase(type: String?): MessageBuilder {
    val res = EnvelopeBuilder()
    if (type != null && !type.isEmpty()) {
        res.setMetaValue(MESSAGE_TYPE_KEY, type)
    }
    return res
}

/**
 * An empty confirmation response without meta and data
 *
 * @param type
 * @return
 */
fun okResponse(type: String): Message {
    return okResponseBase(type, false).build()
}

fun okResponseBase(request: Message, hasMeta: Boolean = true): MessageBuilder {
    val res = EnvelopeBuilder().setMetaValue(MESSAGE_STATUS_KEY, MESSAGE_OK)
    var type: String? = request.meta.getString(MESSAGE_TYPE_KEY, "")
    if (type != null && !type.isEmpty()) {
        if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
            type += RESPONSE_TYPE_SUFFIX
        }
        res.setMetaValue(MESSAGE_TYPE_KEY, type)
    }
    if (hasMeta) {
        res.setMetaValue(RESPONSE_SUCCESS_KEY, true)
    }

    return res
}

/**
 * Confirmation response base
 *
 * @param type
 * @param hasMeta
 * @param hasData
 * @return
 */
fun okResponseBase(type: String?, hasMeta: Boolean = true): MessageBuilder {
    var aType = type
    val res = EnvelopeBuilder()
            .setMetaValue(MESSAGE_STATUS_KEY, MESSAGE_OK)
    if (aType != null && !aType.isEmpty()) {
        if (!aType.endsWith(RESPONSE_TYPE_SUFFIX)) {
            aType += RESPONSE_TYPE_SUFFIX
        }
        res.setMetaValue(MESSAGE_TYPE_KEY, aType)
    }
    if (hasMeta) {
        res.setMetaValue(RESPONSE_SUCCESS_KEY, true)
    }

    return res
}

/**
 * A error response base with given exceptions
 *
 * @param type
 * @param errors
 * @return
 */
fun errorResponseBase(type: String?, vararg errors: Throwable): MessageBuilder {
    var aType = type
    if (aType == null || aType.isEmpty()) {
        aType = ERROR_RESPONSE_TYPE
    }
    if (!aType.endsWith(RESPONSE_TYPE_SUFFIX)) {
        aType += RESPONSE_TYPE_SUFFIX
    }
    val builder = responseBase(aType).setMetaValue(MESSAGE_STATUS_KEY, MESSAGE_FAIL)
    for (err in errors) {
        builder.putMetaNode(getErrorMeta(err))
    }
    return builder.setMetaValue(RESPONSE_SUCCESS_KEY, false)
}

fun errorResponseBase(request: Envelope, vararg errors: Throwable): EnvelopeBuilder {
    return errorResponseBase(request.meta.getString(MESSAGE_TYPE_KEY, ""), *errors)
}


/**
 * Terminator envelope that should be sent to close current connection
 *
 * @return
 */
val terminator: Message = MessageBuilder().setMetaValue(MESSAGE_STATUS_KEY, MESSAGE_TERMINATE).build()

fun isTerminator(message: Message): Boolean {
    return message.meta.getString(MESSAGE_STATUS_KEY, "") == MESSAGE_TERMINATE
}

fun getErrorMeta(err: Throwable): Meta {
    return MetaBuilder("error")
            .putValue("type", err.javaClass.name)
            .putValue("message", err.message)
            .build()
}