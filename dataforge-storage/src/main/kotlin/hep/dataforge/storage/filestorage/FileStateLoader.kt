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

import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.MetaType
import hep.dataforge.io.jsonMetaType
import hep.dataforge.kodex.nullable
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.StateLoader
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.loaders.StateHolder
import hep.dataforge.values.Value
import java.util.stream.Stream

/**
 * A file implementation of state loader
 *
 * @author Alexander Nozik
 */
class FileStateLoader(storage: Storage, name: String, annotation: Meta, file: FileEnvelope) : FileLoader(storage, name, annotation, file), StateLoader {
    val metaType: MetaType = jsonMetaType

    private val loader = object : StateHolder.MetaHandler {

        override val hash: Int
            get() = file.meta.optNumber("metaHash").map { it.toInt() }.orElse(0)

        override fun push(meta: Meta) {
            file.clearData()
            file.append(metaType.writer.writeString(meta).toByteArray())
        }

        override fun pull(): Meta? {
            return metaType.reader.readBuffer(file.data.buffer)
        }

    }

    private val stateHolder = StateHolder(this.connectionHelper, loader)

    override val valueStream: Stream<Pair<String, Value>> = stateHolder.states

    override val metaStream: Stream<Pair<String, Meta>> = stateHolder.metaStates

    override fun push(path: String, value: Value) {
        stateHolder.push(path, value)
    }

    override fun push(path: String, meta: Meta) {
        stateHolder.push(path, meta)
    }

    override fun pull(path: String): Value? {
        return stateHolder.config.optValue(path).nullable
    }

    override fun pullMeta(path: String): Meta? {
        return stateHolder.config.optMeta(path).nullable
    }

    override fun respond(message: Envelope): Envelope {
        return stateHolder.respond(message)
    }
}
