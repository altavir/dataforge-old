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

import hep.dataforge.exceptions.StorageException
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.loaders.AbstractStateLoader
import hep.dataforge.values.Value
import org.apache.commons.io.FilenameUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.regex.Pattern

/**
 * A file implementation of state loader
 *
 * @author Alexander Nozik
 */
class FileStateLoader @Throws(IOException::class, StorageException::class)
constructor(private val path: Path, storage: Storage, name: String, annotation: Meta) : AbstractStateLoader(storage, name, annotation) {
    private var file: FileEnvelope? = null

    override val isOpen: Boolean
        get() = file != null

    @Throws(Exception::class)
    override fun open() {
        if (this.meta == null) {
            this.meta = getFile()!!.meta
        }
        if (!isOpen) {
            file = FileEnvelope.open(path, isReadOnly)
        }
    }

    @Throws(Exception::class)
    override fun close() {
        getFile()!!.close()
        file = null
        super.close()
    }

    @Throws(StorageException::class)
    override fun commit() {
        try {
            getFile()!!.clearData()
            for ((key, value) in states) {
                getFile()!!.append(String.format("%s=%s;\r\n", key, value.stringValue()).toByteArray(Charset.forName("UTF-8")))
            }
        } catch (ex: Exception) {
            throw StorageException(ex)
        }

    }

    @Synchronized
    @Throws(StorageException::class)
    override fun update() {
        try {
            val reader = BufferedReader(InputStreamReader(getFile()!!.data.stream))
            states.clear()
            reader.lines().forEach { line ->
                if (!line.isEmpty()) {
                    val match = Pattern.compile("(?<key>[^=]*)\\s*=\\s*(?<value>.*);").matcher(line)
                    if (match.matches()) {
                        val key = match.group("key")
                        val value = Value.of(match.group("value"))
                        states[key] = value
                    }
                }
            }
            isUpToDate = true
        } catch (ex: Exception) {
            throw StorageException(ex)
        }

    }

    /**
     * @return the file
     */
    @Throws(Exception::class)
    private fun getFile(): FileEnvelope? {
        if (file == null) {
            open()
        }
        return file
    }

    companion object {

        @Throws(Exception::class)
        fun fromEnvelope(storage: Storage, envelope: FileEnvelope): FileStateLoader {
            if (FileStorageEnvelopeType.validate(envelope, StateLoader.STATE_LOADER_TYPE)) {
                val res = FileStateLoader(envelope.file,
                        storage, FilenameUtils.getBaseName(envelope.file.fileName.toString()),
                        envelope.meta)
                res.isReadOnly = envelope.isReadOnly
                return res
            } else {
                throw StorageException("Is not a valid state loader file")
            }
        }
    }
}
