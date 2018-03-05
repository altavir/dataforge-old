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

import hep.dataforge.context.Context
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.api.StorageType
import hep.dataforge.storage.commons.StorageManager

import java.io.File
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Alexander Nozik
 */
class FileStorageFactory : StorageType {

    override fun type(): String {
        return "file"
    }

    override fun build(context: Context, meta: Meta): Storage {
        val path = meta.optString("path").map<URI> { URI.create(it) }.map<Path> { Paths.get(it) }.orElse(context.io.workDir)
        return FileStorage(context, meta, path)
    }

    companion object {

        fun buildStorageMeta(path: URI, readOnly: Boolean, monitor: Boolean): MetaBuilder {
            return MetaBuilder("storage")
                    .setValue("path", path.toString())
                    .setValue("type", "file")
                    .setValue("readOnly", readOnly)
                    .setValue("monitor", monitor)
        }

        fun buildStorageMeta(file: File, readOnly: Boolean, monitor: Boolean): MetaBuilder {
            return buildStorageMeta(file.toURI(), readOnly, monitor)
        }

        /**
         * Build local storage with Global context. Used for tests.
         *
         * @param file
         * @return
         */
        fun buildLocal(context: Context, file: File, readOnly: Boolean, monitor: Boolean): FileStorage {
            val manager = context.load(StorageManager::class.java, Meta.empty())
            return manager.buildStorage(buildStorageMeta(file.toURI(), readOnly, monitor)) as FileStorage
        }
    }

}
