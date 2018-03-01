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
package hep.dataforge.storage.commons

import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder

/**
 * A factory for messages with fixed format
 * (`DATAFORGE_MESSAGE_ENVELOPE_CODE`) and meta properties
 *
 * @author Alexander Nozik
 */
open class MessageFactory {

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
            res.putMetaValue(MESSAGE_TYPE_KEY, type + RESPONSE_TYPE_SUFFIX)
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
        var type = type
        val res = EnvelopeBuilder()
        if (type != null && !type.isEmpty()) {
            if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
                type += RESPONSE_TYPE_SUFFIX
            }
            res.putMetaValue(MESSAGE_TYPE_KEY, type)
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
            res.putMetaValue(MESSAGE_TYPE_KEY, type)
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
        val res = EnvelopeBuilder()
                .putMetaValue(MESSAGE_META_KEY, MESSAGE_OK)
        var type: String? = request.meta.getString(MESSAGE_TYPE_KEY, "")
        if (type != null && !type.isEmpty()) {
            if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
                type += RESPONSE_TYPE_SUFFIX
            }
            res.putMetaValue(MESSAGE_TYPE_KEY, type)
        }
        if (hasMeta) {
            res.putMetaValue(RESPONSE_SUCCESS_KEY, true)
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
        var type = type
        val res = EnvelopeBuilder()
                .putMetaValue(MESSAGE_META_KEY, MESSAGE_OK)
        if (type != null && !type.isEmpty()) {
            if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
                type += RESPONSE_TYPE_SUFFIX
            }
            res.putMetaValue(MESSAGE_TYPE_KEY, type)
        }
        if (hasMeta) {
            res.putMetaValue(RESPONSE_SUCCESS_KEY, true)
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
        var type = type
        if (type == null || type.isEmpty()) {
            type = ERROR_RESPONSE_TYPE
        }
        if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
            type += RESPONSE_TYPE_SUFFIX
        }
        val builder = responseBase(type)
                .putMetaValue(MESSAGE_META_KEY, MESSAGE_FAIL)
        for (err in errors) {
            builder.putMetaNode(StorageUtils.getErrorMeta(err))
        }
        return builder.putMetaValue(RESPONSE_SUCCESS_KEY, false)
    }

    fun errorResponseBase(request: Envelope, vararg errors: Throwable): EnvelopeBuilder {
        return errorResponseBase(request.meta.getString(MESSAGE_TYPE_KEY, ""), *errors)
    }

    companion object {

        const val MESSAGE_META_KEY = "message"
        const val MESSAGE_TERMINATOR = "@terminate"
        const val MESSAGE_OK = "@ok"
        const val MESSAGE_FAIL = "@error"

        const val MESSAGE_TYPE_KEY = "type"
        const val RESPONSE_SUCCESS_KEY = "success"
        const val RESPONSE_TYPE_SUFFIX = ".response"
        const val ERROR_RESPONSE_TYPE = "error"

        /**
         * Terminator envelope that should be sent to close current connection
         *
         * @return
         */
        fun terminator(): Envelope {
            return EnvelopeBuilder().putMetaValue(MESSAGE_META_KEY, MESSAGE_TERMINATOR).build()
        }

        fun isTerminator(envelope: Envelope): Boolean {
            return envelope.meta.getString(MESSAGE_META_KEY, "") == MESSAGE_TERMINATOR
        }
    }

}
