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

import hep.dataforge.io.envelopes.*
import hep.dataforge.meta.Meta
import hep.dataforge.tables.*
import hep.dataforge.values.ValueMap
import hep.dataforge.values.ValueUtils
import hep.dataforge.values.Values
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.ObjectInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.coroutines.experimental.buildSequence
import kotlin.streams.asSequence
import kotlin.streams.toList

interface TableLoader : IndexedLoader<Int, Values>, ValuesSource {
    val format: TableFormat
}

abstract class AbstractFileTableLoader(parent: StorageElement, path: Path) : EnvelopeLoader<Values>(
        name = "hep.dataforge.storage.table",
        type = Values::class,
        parent = parent,
        path = path
), TableLoader {
    protected val index: TreeMap<Int, Int>

    override val keys: NavigableSet<Int>
        get() = index.navigableKeySet()

    protected abstract suspend fun readAt(offset: Int): Values

    override fun getInFuture(key: Int): Deferred<Values>? {
        return index[key]?.let {  async { readAt(it) }}
    }

    override val format: TableFormat by lazy {
        when {
            meta.hasMeta("format") -> MetaTableFormat(meta.getMeta("format"))
            meta.hasValue("format") -> MetaTableFormat.forNames(meta.getStringArray("format"))
            else -> throw RuntimeException("Format definition not found")
        }
    }
}

class FileTextTableLoader(parent: StorageElement, path: Path) : AbstractFileTableLoader(parent, path) {

    private val parser: ValuesParser by lazy {
        SimpleValuesParser(format)
    }

    override suspend fun readAt(offset: Int): Values {
        //FIXME ineffective call
        return parser.parse(data.stream(offset.toLong()).bufferedReader(Charsets.UTF_8).readLine())
    }

    override fun readAll(): Sequence<Values> {
        val reader = data.stream.bufferedReader(Charsets.UTF_8)
        return reader.lines().asSequence().map { parser.parse(it) }
    }

}

class FileBinaryTableLoader(parent: StorageElement, path: Path) : AbstractFileTableLoader(parent, path) {
    private fun read(stream: ObjectInputStream): Values {
        return ValueMap(format.columns.map { it.name to ValueUtils.readValue(stream) }.toList().toMap())
    }

    override suspend fun readAt(offset: Int): Values {
        return ObjectInputStream(data.stream(offset.toLong())).use(this::read)
    }

    override fun readAll(): Sequence<Values> {
        return buildSequence {
            ObjectInputStream(data.stream).use { stream ->
                while (stream.available() > 0) {
                    yield(read(stream))
                    //read line terminator (\n)
                    stream.read()
                }
            }
        }
    }
}


object TableLoaderType : FileStorageElementType<EnvelopeLoader<Values>> {
    const val TABLE_ENVELOPE_TYPE = "hep.dataforge.storage.table"
    const val BINARY_DATA_TYPE = "binary"
    const val TEXT_DATA_TYPE = "text"

    override suspend fun create(parent: FileStorage, meta: Meta): EnvelopeLoader<Values> {
        if (!meta.hasMeta("format")) {
            throw IllegalArgumentException("Values format not found")
        }
        val fileName = meta.getString("name")
        val path: Path = parent.path.resolve("$fileName.df")

        val envelope = EnvelopeBuilder()
                .setEnvelopeType(TABLE_ENVELOPE_TYPE)
                .setMeta(meta)
                .build()

        return Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
            when (meta.getString(Envelope.ENVELOPE_DATA_TYPE_KEY, "binary")) {
                BINARY_DATA_TYPE -> {
                    DefaultEnvelopeType.INSTANCE.writer.write(it, envelope)
                    FileBinaryTableLoader(parent, path)
                }
                TEXT_DATA_TYPE -> {
                    TaglessEnvelopeType.INSTANCE.writer.write(it, envelope)
                    FileTextTableLoader(parent, path)
                }
                else -> throw RuntimeException("Unknown data type for table loader")
            }
        }
    }

    override suspend fun read(parent: FileStorage, path: Path): EnvelopeLoader<Values> {
        val envelope = EnvelopeReader.readFile(path)
        return when (envelope.dataType) {
            BINARY_DATA_TYPE -> FileBinaryTableLoader(parent, path)
            TEXT_DATA_TYPE -> FileTextTableLoader(parent, path)
            else -> throw RuntimeException("Unknown data type for table loader")
        }
    }
}