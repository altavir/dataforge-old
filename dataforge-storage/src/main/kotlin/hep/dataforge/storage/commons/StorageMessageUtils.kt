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
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import java.nio.ByteBuffer

/**
 * A delegate to evaluate messages for loaders
 *
 * @author Alexander Nozik
 */
object StorageMessageUtils {

    val ACTION_KEY = "action"

    val QUERY_ELEMENT = "query"


    //Message operations
    val PUSH_OPERATION = "push"
    val PULL_OPERATION = "pull"

    /**
     * Create a default 'OK' response for push request
     *
     * @param request
     * @return
     */
    fun confirmationResponse(request: Envelope): Envelope {
        val meta = MetaBuilder("response")
                .putValue("success", true)
                .build()

        return EnvelopeBuilder(request)
                .setMeta(meta)
                .build()

    }

    fun exceptionResponse(request: Envelope, vararg exceptions: Throwable): Envelope {
        val meta = MetaBuilder("response")
                .putValue("success", false)

        for (exception in exceptions) {
            val ex = MetaBuilder("error")
                    .putValue("type", exception.javaClass.typeName)
                    .putValue("message", exception.message)
            meta.putNode(ex)
        }

        return EnvelopeBuilder(request)
                .setMeta(meta.build())
                .build()
    }

    fun response(request: Envelope, response: Meta, data: ByteBuffer): Envelope {
        throw UnsupportedOperationException()
    }

}
