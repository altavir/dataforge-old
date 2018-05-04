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
import hep.dataforge.tables.MetaTableFormat
import hep.dataforge.tables.TableFormat
import hep.dataforge.tables.ValuesSource
import hep.dataforge.values.Value
import hep.dataforge.values.Values
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.coroutines.experimental.buildSequence

interface TableLoader : Loader<Values>, ValuesSource {
    val format: TableFormat
    fun indexed(meta: Meta = Meta.empty()): IndexedTableLoader
    fun mutable(): MutableTableLoader
}

interface IndexedTableLoader : TableLoader, IndexedLoader<Value, Values> {
    operator fun get(any: Any): Values? = get(Value.of(any))
}

interface MutableTableLoader : TableLoader, AppendableLoader<Values>


/**
 * @param reader read Values and move buffer position to next entry
 */
open class FileTableLoader(
        parent: StorageElement,
        path: Path,
        val reader: (ByteBuffer, TableFormat) -> Values
) : EnvelopeLoader<Values>(
        name = "hep.dataforge.storage.table",
        type = Values::class,
        parent = parent,
        path = path
), TableLoader {
    override val format: TableFormat by lazy {
        when {
            meta.hasMeta("format") -> MetaTableFormat(meta.getMeta("format"))
            meta.hasValue("format") -> MetaTableFormat.forNames(meta.getStringArray("format"))
            else -> throw RuntimeException("Format definition not found")
        }
    }

    override fun indexed(meta: Meta): IndexedTableLoader {
        if (this is IndexedTableLoader) {
            return this
        } else {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun mutable(): MutableTableLoader {
        if (this is MutableTableLoader) {
            return this
        } else {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun readAll(): Sequence<Pair<Int, Values>> {
        val buffer = data.buffer
        buffer.position(0)
        return buildSequence {
            while (buffer.remaining() > 0) {
                yield(Pair(buffer.position(), reader(buffer, format)))
            }
        }
    }
}

object TableLoaderType : FileStorageElementType<EnvelopeLoader<Values>> {
    const val TABLE_ENVELOPE_TYPE = "hep.dataforge.storage.table"
    const val BINARY_DATA_TYPE = "binary"
    const val TEXT_DATA_TYPE = "text"


    private val textTableReader: (ByteBuffer, TableFormat) -> Values = {
        
    }

    private val binaryTableReader: (ByteBuffer, TableFormat) -> Values = {

    }

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
                    FileTableLoader(parent, path, binaryTableReader)
                }
                TEXT_DATA_TYPE -> {
                    TaglessEnvelopeType.INSTANCE.writer.write(it, envelope)
                    FileTableLoader(parent, path, textTableReader)
                }
                else -> throw RuntimeException("Unknown data type for table loader")
            }
        }
    }

    override suspend fun read(parent: FileStorage, path: Path): EnvelopeLoader<Values> {
        val envelope = EnvelopeReader.readFile(path)
        return when (envelope.dataType) {
            BINARY_DATA_TYPE -> FileTableLoader(parent, path, binaryTableReader)
            TEXT_DATA_TYPE -> FileTableLoader(parent, path, textTableReader)
            else -> throw RuntimeException("Unknown data type for table loader")
        }
    }
}