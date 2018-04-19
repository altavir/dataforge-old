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
package hep.dataforge.storage.filestorage

import hep.dataforge.exceptions.StorageException
import hep.dataforge.io.envelopes.DefaultEnvelopeReader
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.jsonMetaType
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.EnvelopeLoader
import hep.dataforge.storage.api.Storage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 *
 * @author Alexander Nozik
 */
class FileEnvelopeLoader(storage: Storage, name: String, meta: Meta, file: FileEnvelope) : FileLoader(storage, name, meta, file), EnvelopeLoader {

    @Throws(StorageException::class)
    override fun push(env: Envelope) {
        if (!isReadOnly) {
            try {
                val baos = ByteArrayOutputStream()
                DefaultEnvelopeWriter(FileStorageEnvelopeType, jsonMetaType).write(baos, env)
                file.append(baos.toByteArray())
            } catch (ex: IOException) {
                throw StorageException("Can't push envelope to loader", ex)
            }

        } else {
            throw StorageException("The loader is read only")
        }
    }

    override fun iterator(): Iterator<Envelope> {
        val st: InputStream
        try {
            st = file.data.stream
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }

        return object : Iterator<Envelope> {
            override fun hasNext(): Boolean {
                try {
                    return st.available() > 0
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }

            }

            override fun next(): Envelope {
                try {
                    return DefaultEnvelopeReader.INSTANCE.readWithData(st)
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }

            }
        }
    }

}
