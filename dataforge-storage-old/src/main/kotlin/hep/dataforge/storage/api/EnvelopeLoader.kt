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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.api

import hep.dataforge.exceptions.StorageException
import hep.dataforge.io.envelopes.Envelope

/**
 * A segmented loader containing an ordered set of envelopes
 *
 * @author Alexander Nozik
 */
interface EnvelopeLoader : Loader, Iterable<Envelope> {

    override val type: String
        get() = ENVELOPE_LOADER_TYPE

    /**
     * Push new envelope to loader
     *
     * @param env
     * @throws StorageException
     */
    fun push(env: Envelope)

    companion object {
        const val ENVELOPE_LOADER_TYPE = "envelope"
    }
}
