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

package hep.dataforge.storage

import hep.dataforge.connections.ConnectionHelper
import hep.dataforge.context.Context
import hep.dataforge.data.binary.Binary
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeReader
import hep.dataforge.meta.Meta
import java.nio.file.Path
import kotlin.reflect.KClass

abstract class EnvelopeLoader<T : Any> protected constructor(
        final override val name: String,
        final override val type: KClass<T>,
        final override val parent: StorageElement,
        final override val path: Path
) : Loader<T>, FileStorageElement {
    private val _connectionHelper = ConnectionHelper(this)
    protected val envelope: Envelope by lazy {
        EnvelopeReader.readFile(path)
    }

    override val meta: Meta
        get() = envelope.meta

    override val context: Context = parent.context

    override fun getConnectionHelper(): ConnectionHelper = _connectionHelper

    val data: Binary
        get() = envelope.data

    protected abstract fun readAll(): Sequence<T>

    override fun iterator(): Iterator<T> = readAll().iterator()

    override fun close() {
        //do nothing
    }
}