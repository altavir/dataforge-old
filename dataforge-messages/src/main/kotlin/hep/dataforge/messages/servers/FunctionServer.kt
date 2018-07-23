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

package hep.dataforge.messages.servers

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.messages.Message
import hep.dataforge.messages.Responder
import hep.dataforge.messages.okResponseBase
import hep.dataforge.meta.Meta
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


abstract class BinaryServer(override val context: Context) : Responder, ContextAware {

    abstract val chunkSize: Int

    abstract fun evaluate(chunk: ByteBuffer, meta: Meta): ByteBuffer

    override fun respond(message: Message): Message {
        val buffer = ByteBuffer.allocate(chunkSize)
        val out = ByteArrayOutputStream()
        val response = message.data.channel.use {
            do {
                val bytesRead = it.read(buffer)
                if (bytesRead != 0 && bytesRead < chunkSize) {
                    throw RuntimeException("Can't read $chunkSize bytes from message")
                }
                val res = evaluate(buffer, message.meta)
                out.write(res.array())

            } while (bytesRead != 0)
        }
        return okResponseBase(message).data(out.toByteArray()).build()
    }
}