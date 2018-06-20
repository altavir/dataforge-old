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

import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.loaders.AbstractLoader


abstract class FileLoader(storage: Storage, name: String, meta: Meta, val file: FileEnvelope) : AbstractLoader(storage, name, meta) {
    override val isEmpty: Boolean
        get() = !file.hasData()

    override val isOpen: Boolean
        get() = file.isOpen

    override val isReadOnly: Boolean
        get() = file.isReadOnly

    override val meta: Laminate by lazy {
        Laminate(super.meta, file.meta)
    }

    @Throws(Exception::class)
    override fun close() {
        file.close()
    }
}