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

import hep.dataforge.data.binary.Binary
import hep.dataforge.data.binary.BufferedBinary
import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.io.envelopes.*
import hep.dataforge.meta.Meta
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A file i/o for envelope-based format
 */
abstract class FileEnvelope(val channel: FileChannel, private val dataOffset: Long, protected var dataLength: Int = 0) : Envelope {

    /**
     * Read the whole data block
     */
    override val data: Binary
        get() = BufferedBinary(ByteBuffer.allocate(dataLength).also { channel.read(it, dataOffset) })

    protected open fun updateDataLength(length: Int) {
        dataLength = length
    }

    /**
     * Append data to the end of envelope file and update tag
     */
    fun append(buffer: ByteBuffer) {
        synchronized(this) {
            updateDataLength(dataLength + channel.write(buffer, (dataOffset + dataLength)))
        }
    }

    /**
     * Clear data of envelope file and update tag
     */
    fun clearData() {
        synchronized(this) {
            channel.truncate(dataOffset)
            updateDataLength(0)
        }
    }

    /**
     * Clear and replace data
     */
    fun replaceData(buffer: ByteBuffer) {
        clearData()
        append(buffer)
    }

    /**
     * Read data block in given position
     */
    fun read(pos: Long, length: Int): ByteBuffer {
        return ByteBuffer.allocate(length).also { channel.read(it, pos) }
    }

    companion object {

        /**
         * Read existing file as FileEnvelope
         */
        fun readExisting(path: Path): FileEnvelope {
            if (Files.exists(path)) {
                val channel = FileChannel.open(path)
                val type = EnvelopeType.infer(path).orElse(TaglessEnvelopeType.INSTANCE)
                when (type) {
                    is DefaultEnvelopeType -> {
                        val tag = EnvelopeTag().read(channel)
                        return TaggedFileEnvelope(channel, tag)
                    }
                    is TaglessEnvelopeType -> {
                        TODO("Implement for tagless envelope")
                        //Files.lines(path, Charsets.UTF_8).asSequence().takeWhile { it.trim() == TaglessEnvelopeType.DEFAULT_DATA_START }
                        //return FileEnvelope(channel)
                    }
                    else -> throw RuntimeException("Envelope type ${type.name} could not be read")
                }
            } else {
                throw RuntimeException("File $path does not exist")
            }
        }

        /**
         * Create new Envelope-based file. Throw exception if it already exists.
         * @param path path of the file
         * @param meta meta for the envelope
         * @param properties additional properties for envelope
         */
        @Synchronized
        fun createNew(path: Path, meta: Meta, properties: Map<String, String> = emptyMap()): FileEnvelope {
            val type = EnvelopeType.resolve(properties.getOrDefault(Envelope.ENVELOPE_TYPE_KEY, DefaultEnvelopeType.DEFAULT_ENVELOPE_NAME))
                    ?: throw NameNotFoundException("Can't resolve envelope type")
            Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
                type.getWriter(properties).write(it, SimpleEnvelope(meta))
            }
            return readExisting(path)
        }
    }
}

class TaggedFileEnvelope(channel: FileChannel, private val tag: EnvelopeTag) : FileEnvelope(channel, (tag.length + tag.metaSize).toLong(), tag.dataSize) {
    override val meta: Meta by lazy {
        val buffer = ByteBuffer.allocate(tag.metaSize).also {
            channel.read(it, tag.length.toLong())
        }
        tag.metaType.reader.readBuffer(buffer)
    }

    override fun updateDataLength(length: Int) {
        if (dataLength > Int.MAX_VALUE) {
            throw RuntimeException("Too large data block")
        }
        tag.dataSize = length
        channel.write(tag.toBytes(), 0L)
    }
}