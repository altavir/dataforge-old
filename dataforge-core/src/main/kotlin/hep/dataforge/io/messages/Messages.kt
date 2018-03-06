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


/**
 * Generate a response base with the same meta parameters (type, encoding)
 * as in request and modified message type if it is present
 *
 * @param request
 * @return
 */
fun responseBase(request: Envelope): EnvelopeBuilder {
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
fun responseBase(type: String?): EnvelopeBuilder {
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
fun requestBase(type: String?): EnvelopeBuilder {
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
fun okResponse(type: String): Envelope {
    return okResponseBase(type, false, false).build()
}

fun okResponseBase(request: Envelope, hasMeta: Boolean, hasData: Boolean): EnvelopeBuilder {
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
fun okResponseBase(type: String?, hasMeta: Boolean, hasData: Boolean): EnvelopeBuilder {
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
fun errorResponseBase(type: String?, vararg errors: Throwable): EnvelopeBuilder {
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

/**
 * Terminator envelope that should be sent to close current connection
 *
 * @return
 */
val terminator: Envelope = EnvelopeBuilder().setMetaValue(MESSAGE_STATUS_KEY, MESSAGE_TERMINATE).build()

fun isTerminator(envelope: Envelope): Boolean {
    return envelope.meta.getString(MESSAGE_STATUS_KEY, "") == MESSAGE_TERMINATE
}

fun getErrorMeta(err: Throwable): Meta {
    return MetaBuilder("error")
            .putValue("type", err.javaClass.name)
            .putValue("message", err.message)
            .build()
}