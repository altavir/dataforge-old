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
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.ObjectLoader
import hep.dataforge.storage.api.Storage
import org.apache.commons.io.FilenameUtils
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

/**
 * A file loader to store Serializable java objects
 *
 * @author Alexander Nozik
 */
class FileObjectLoader<T : Serializable>(storage: Storage, name: String, meta: Meta, file:FileEnvelope) : FileLoader(storage, name, meta, file), ObjectLoader<T> {
    private val dataMap = HashMap<String, T>()

    @Throws(StorageException::class)
    fun getDataMap(): MutableMap<String, T> {
        if (dataMap.isEmpty()) {
            dataMap.putAll(readDataMap())
        }
        return dataMap
    }

    @Synchronized
    @Throws(StorageException::class)
    protected fun readDataMap(): Map<String, T> {
        try {
            ObjectInputStream(file.data.stream).use { ois -> return ois.readObject() as Map<String, T> }
        } catch (ex: Exception) {
            return HashMap()
            //throw new StorageException(ex);
        }

    }

    @Synchronized
    @Throws(StorageException::class)
    protected fun writeDataMap(data: Map<String, T>) {
        val baos = ByteArrayOutputStream()
        try {
            ObjectOutputStream(baos).use { oos ->
                oos.writeObject(data)
                file.clearData()
                file.append(baos.toByteArray())
            }
        } catch (ex: Exception) {
            throw StorageException(ex)
        }

    }
    override fun fragmentNames(): Collection<String> {
        try {
            return getDataMap().keys
        } catch (ex: Exception) {
            return emptyList()
        }

    }

    @Throws(StorageException::class)
    override fun pull(fragmentName: String): T {
        return getDataMap()[fragmentName]
    }

    @Throws(StorageException::class)
    override fun push(fragmentName: String, data: T) {
        getDataMap()[fragmentName] = data
        writeDataMap(dataMap)
    }

    companion object {

        @Throws(Exception::class)
        fun <T : Serializable> fromEnvelope(storage: Storage, envelope: FileEnvelope): FileObjectLoader<T> {
            if (FileStorageEnvelopeType.validate(envelope, ObjectLoader.OBJECT_LOADER_TYPE)) {
                val res = FileObjectLoader(storage,
                        FilenameUtils.getBaseName(envelope.file.fileName.toString()),
                        envelope.meta,
                        envelope.file)
                res.isReadOnly = envelope.isReadOnly
                return res
            } else {
                throw StorageException("Is not a valid object loader file")
            }
        }
    }

}
