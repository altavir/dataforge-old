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

package hep.dataforge.maths.functions

import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.io.messages.Responder
import hep.dataforge.meta.Meta
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

interface RemoteFunction {
    suspend operator fun invoke(key: String, meta: Meta, arguments: List<Double>): Double
}

abstract class ServerFunction: AutoCloseable {

    /**
     * A master parent job governing cancellation of all existing channels
     */
    private val masterJob = Job()

    abstract val chunkSize: Int

    /**
     * Invoke function on input buffer and return output buffer
     */
    abstract suspend operator fun invoke(buffer: ByteBuffer): ByteBuffer

    /**
     * create a channel which asynchronously reads arguments from input channel and writes them to the output channel
     */
    fun channel(input: ReadableByteChannel, output: WritableByteChannel) {
        launch(parent = masterJob) {
            while(true) {
                val x = readChannel(input)
                val y = invoke(x)
                output.write(y)
            }
        }
    }

    override fun close() {
        masterJob.cancel(CancellationException("Server is closed"))
    }
}

class UnivariateServerFunction(val function: (Double)-> Double): ServerFunction() {
    override suspend fun invoke(buffer: ByteBuffer): ByteBuffer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readChannel(input: ReadableByteChannel): ByteBuffer {
        return input.re
    }

}

class SimpleRemoteFunction(val responder: Responder) : RemoteFunction {
    override suspend operator fun invoke(key: String, meta: Meta, arguments: List<Double>): Double {
        val request = EnvelopeBuilder()
                .setContentType("hep.dataforge.function.request")
                .setMetaValue("action", "getValue")
                .setMetaValue("request.key", key)
                .putMetaNode("request.meta", meta)
                .setMetaValue("request.argument", arguments)
                .build()

        val response = async {
            responder.respond(request)
        }

        return response.await().meta.getDouble("result")
    }

}