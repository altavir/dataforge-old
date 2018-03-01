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
package hep.dataforge.storage.commons

import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.EnvelopeLoader
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.loaders.AbstractLoader

/**
 *
 * @author Alexander Nozik
 */
abstract class AbstractEnvelopeLoader(storage: Storage, name: String, meta: Meta) : AbstractLoader(storage, name, meta), EnvelopeLoader {

    override val type: String = EnvelopeLoader.ENVELOPE_LOADER_TYPE

    override fun respond(message: Envelope): Envelope {
        throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
    }


}
