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
import hep.dataforge.kodex.buildMeta
import hep.dataforge.meta.Meta
import hep.dataforge.storage.TableLoaderType.BINARY_DATA_TYPE
import hep.dataforge.storage.TableLoaderType.TABLE_FORMAT_KEY
import hep.dataforge.storage.TableLoaderType.TEXT_DATA_TYPE
import hep.dataforge.storage.TableLoaderType.binaryTableWriter
import hep.dataforge.tables.MetaTableFormat
import hep.dataforge.tables.TableFormat
import hep.dataforge.tables.ValuesSource
import hep.dataforge.values.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.coroutines.experimental.buildSequence

interface TableLoader : Loader<Values>, ValuesSource {
    val format: TableFormat
    fun indexed(meta: Meta = Meta.empty()): IndexedTableLoader
    fun mutable(): MutableTableLoader
}

interface IndexedTableLoader : TableLoader, IndexedLoader<Value, Values> {
    operator fun get(any: Any): Values? = get(Value.of(any))

    /**
     * Notify loader that it should update index for this loader
     */
    suspend fun updateIndex()

    fun select(from: Value, to: Value): List<Values> {
        return keys.subSet(from, true, to, true).map { get(it)!! }
    }
}

interface MutableTableLoader : TableLoader, AppendableLoader<Values>


/**
 * @param reader read Values and move buffer position to next entry
 */
open class FileTableLoader internal constructor(
        parent: StorageElement?,
        path: Path,
        val reader: (ByteBuffer, TableFormat) -> Values
) : EnvelopeLoader<Values>(
        name = "hep.dataforge.storage.table",
        type = Values::class,
        parent = parent,
        path = path
), IndexedTableLoader {

    override val format: TableFormat by lazy {
        when {
            meta.hasMeta(TABLE_FORMAT_KEY) -> MetaTableFormat(meta.getMeta("format"))
            meta.hasValue(TABLE_FORMAT_KEY) -> MetaTableFormat.forNames(*meta.getStringArray("format"))
            else -> throw RuntimeException("Format definition not found")
        }
    }

    protected val defaultIndex = TreeMap<Value, Int>()

    protected fun getOffset(index: Int): Int? {
        if (index == 0) {
            return 0
        } else if (index >= defaultIndex.size) {
            readAll(defaultIndex.size)
        }
        return defaultIndex[index.asValue()]
    }

    override val keys: NavigableSet<Value>
        get() = synchronized(defaultIndex) { defaultIndex.navigableKeySet() }

    override fun getInFuture(key: Value): Deferred<Values>? {
        return getOffset(key.int)?.let {
            async {
                synchronized(data) {
                    reader(data.buffer.apply { position(it) }, format)
                }
            }
        }
    }

    override fun mutable(): AppendableFileTableLoader {
        return when (meta.getString(Envelope.ENVELOPE_DATA_TYPE_KEY)) {
            BINARY_DATA_TYPE -> AppendableFileTableLoader(this, binaryTableWriter)
            TEXT_DATA_TYPE -> error("Text output is no longer supported")
            else -> error("Unsupported table serialization format")
        }
    }

    override fun indexed(meta: Meta): IndexedTableLoader {
        return if (meta.isEmpty) {
            this
        } else {
            IndexedFileTableLoader(this, meta.getString("field"))
        }
    }

    override suspend fun updateIndex() {
        if(defaultIndex.isEmpty()){
            readAll()
        } else {
            readAll(defaultIndex.lastKey().int)
        }
    }


    override fun readAll(startIndex: Int): Sequence<Triple<Int, Int, Values>> {
        val offset = getOffset(startIndex) ?: throw Error("The index value is unavailable")
        synchronized(data) {
            var counter = startIndex
            val buffer = data.buffer
            buffer.position(offset)
            return buildSequence {
                while (buffer.remaining() > 0) {
                    defaultIndex.putIfAbsent(counter.asValue(), buffer.position())
                    yield(Triple(counter, buffer.position(), reader(buffer, format)))
                    counter++
                }
            }
        }
    }
}

/**
 * File table loader with alternate index
 */
class IndexedFileTableLoader(val loader: FileTableLoader, val indexField: String) : IndexedTableLoader by loader {

    //TODO implement index caching
    private val secondaryIndex: TreeMap<Value, Value> by lazy {
        TreeMap<Value, Value>().apply {
            loader.forEachIndexed { index, values ->
                this[values.getValue(indexField)] = index.asValue()
            }
        }
    }

    override suspend fun updateIndex() {
        loader.forEachIndexed { index, values ->
            secondaryIndex[values.getValue(indexField)] = index.asValue()
        }
    }

    override fun getInFuture(key: Value): Deferred<Values>? {
        return secondaryIndex[key]?.let { loader.getInFuture(it) }
    }
}

class AppendableFileTableLoader(val loader: FileTableLoader, val writer: (Values, TableFormat) -> ByteBuffer) : IndexedTableLoader by loader, MutableTableLoader {
    private val mutableEnvelope = FileEnvelope.readExisting(loader.path)

    override suspend fun append(item: Values) {
        mutableEnvelope.append(writer(item, format))
        loader.updateIndex()
    }

    suspend fun append(vararg values: Any) {
        append(ValueMap.of(format.namesAsArray(), *values))
    }

    override fun close() {
        mutableEnvelope.close()
    }
}

object TableLoaderType : FileStorageElementType<EnvelopeLoader<Values>> {
    const val TABLE_ENVELOPE_TYPE = "hep.dataforge.storage.table"
    const val BINARY_DATA_TYPE = "binary"
    const val TEXT_DATA_TYPE = "text"

    const val TABLE_FORMAT_KEY = "format"


    val textTableReader: (ByteBuffer, TableFormat) -> Values = { buffer, format ->
        val line = buildString {
            do {
                val char = buffer.get().toChar()
                append(char)
            } while (char != '\n')
        }
        val values = line.split("\\s+").map { LateParseValue(it) }
        ValueMap(format.names.zip(values).toMap())
    }

    val binaryTableReader: (ByteBuffer, TableFormat) -> Values = { buffer, format ->
        ValueMap(format.names.associate { it to buffer.getValue() }.toMap()).also {
            do {
                val char = buffer.get().toChar()
            } while (char != '\n')
        }
    }

    val textTableWriter: (Values, TableFormat) -> ByteBuffer = { values, format ->
        val string = format.names.map { values[it] }.joinToString(separator = "\t", postfix = "\n")
        ByteBuffer.wrap(string.toByteArray(Charsets.UTF_8))
    }

    val binaryTableWriter: (Values, TableFormat) -> ByteBuffer = { values, format ->
        //TODO Fix the way value is serialized
        val buffer = ByteBuffer.allocate(256)
        format.names.map { values[it] }.forEach { buffer.putValue(it) }
        buffer.put('\n'.toByte())
        buffer.limit(buffer.position())
        buffer.position(0)
        buffer
    }

    override suspend fun create(parent: FileStorage, meta: Meta): FileTableLoader {
        if (!meta.hasMeta(TABLE_FORMAT_KEY)) {
            throw IllegalArgumentException("Values format not found")
        }
        val fileName = meta.getString("name")
        val path: Path = parent.path.resolve("$fileName.df")
        return create(parent, path, meta)
    }

    suspend fun create(parent: FileStorage?, path: Path, meta: Meta): FileTableLoader {
        val type = meta.getString(Envelope.ENVELOPE_DATA_TYPE_KEY, BINARY_DATA_TYPE)

        val envelope = EnvelopeBuilder()
                .setEnvelopeType(TABLE_ENVELOPE_TYPE)
                .setMeta(meta)
                .build()

        return Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
            when (type) {
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

    suspend fun create(parent: FileStorage?, path: Path, format: TableFormat, binary: Boolean = true): FileTableLoader {
        return create(parent, path, buildMeta {
            TABLE_FORMAT_KEY to format.toMeta()
            Envelope.ENVELOPE_DATA_TYPE_KEY to if (binary) {
                BINARY_DATA_TYPE
            } else {
                TEXT_DATA_TYPE
            }
        })
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