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

import hep.dataforge.events.Event
import hep.dataforge.exceptions.StorageException
import hep.dataforge.io.IOUtils
import hep.dataforge.io.JSONMetaWriter
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.EventLoader
import hep.dataforge.storage.api.Storage
import java.util.function.Predicate

/**
 * @author darksnake
 */
class FileEventLoader(storage: Storage, name: String, annotation: Meta, file: FileEnvelope) : FileLoader(storage, name, annotation, file), EventLoader {

    private val NEWLINE = "\r\n".toByteArray()

    private var filter: Predicate<Event>? = null


    /**
     * Set filter that should be applied to events that should be written to
     * file
     *
     * @param filter
     */
    fun setFilter(filter: Predicate<Event>) {
        this.filter = filter
    }

    @Throws(StorageException::class)
    override fun pushEvent(event: Event): Boolean {
        return if (filter?.test(event) != false) {
            try {
                val eventString = JSONMetaWriter.writeString(event.meta)
                file.append(eventString.toByteArray(IOUtils.UTF8_CHARSET))
                file.append(NEWLINE)
                true
            } catch (ex: Exception) {
                throw StorageException(ex)
            }

        } else {
            false
        }
    }

    override fun iterator(): Iterator<Event> {
        throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
    }
}
