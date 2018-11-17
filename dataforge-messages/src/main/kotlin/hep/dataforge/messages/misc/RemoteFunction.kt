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

package hep.dataforge.messages.misc

import hep.dataforge.messages.MessageBuilder
import hep.dataforge.messages.Responder
import hep.dataforge.meta.Meta

interface RemoteFunction {
    suspend operator fun invoke(key: String, meta: Meta, arguments: List<Double>): Double
}

class SimpleRemoteFunction(val responder: Responder): RemoteFunction {
    override suspend operator fun invoke(key: String, meta: Meta, arguments: List<Double>): Double {
        val request = MessageBuilder()
                .setDataType("hep.dataforge.function.request")
                .setMetaValue("action", "getValue")
                .setMetaValue("request.key",key)
                .putMetaNode("request.meta", meta)
                .setMetaValue("request.argument",arguments)
                .build()

        val response = async {
            responder.respond(request)
        }

        return response.await().meta.getDouble("result")
    }

}