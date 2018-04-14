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
package hep.dataforge.storage.filestorage

import hep.dataforge.io.envelopes.DefaultEnvelopeType
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.storage.commons.jsonMetaType

/**
 * An envelope type for storage binaries. Infinite data allowed
 * @author darksnake
 */
object FileStorageEnvelopeType : DefaultEnvelopeType() {
    const val FILE_STORAGE_ENVELOPE_TYPE = "storage"

    override val name: String = FILE_STORAGE_ENVELOPE_TYPE

    override val writer: DefaultEnvelopeWriter = DefaultEnvelopeWriter(this, jsonMetaType)

    override//DFST
    val code: Int
        get() = 0x44465354

    override fun description(): String {
        return "DataForge file storage envelope"
    }

    override fun infiniteMetaAllowed(): Boolean {
        return false
    }

    override fun infiniteDataAllowed(): Boolean {
        return true
    }

    /**
     * Check that declared envelope content type is a storage or empty
     * @param envelope
     * @return
     */
    fun validate(envelope: Envelope): Boolean {
        return envelope.type ?: (FILE_STORAGE_ENVELOPE_TYPE) == FILE_STORAGE_ENVELOPE_TYPE
    }

    fun validate(envelope: Envelope, loaderType: String): Boolean {
        return validate(envelope) && envelope.meta.getString("type", "") == loaderType
    }

}
