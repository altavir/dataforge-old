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

package hep.dataforge.remote

import hep.dataforge.io.envelopes.Envelope

interface RemoteConnection {
    val client: Worker
    val server: Worker

    val state: ConnectionState

    /**
     * Send message to remote worker. Throw message only if sending is failed
     */
    suspend fun send(message: Envelope)

    suspend fun request(message: Envelope): Envelope

    enum class ConnectionState{
        OPEN,
        ESTABLISHING,
        CLOSED,
        BROKEN
    }
}