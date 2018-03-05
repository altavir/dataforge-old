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

import hep.dataforge.data.binary.FileBinary
import hep.dataforge.io.envelopes.*
import hep.dataforge.meta.Meta
import hep.dataforge.storage.commons.jsonMetaType
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectStreamException
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.*

/**
 * A specific envelope to handle file storage format.
 *
 * @author Alexander Nozik
 */
open class FileEnvelope protected constructor(val file: Path, val isReadOnly: Boolean = true, val metaType: MetaType = jsonMetaType) : Envelope, AutoCloseable {

    //TODO redo file envelopes
    private var isOpenForRead: Boolean = false
    private var isOpenForWrite: Boolean = false

    val isOpen: Boolean
        get() = isOpenForRead || isOpenForWrite

    private lateinit var _readChannel: FileChannel
    private val readChannel: FileChannel
        get(){
        if(!isOpenForRead || !_readChannel.isOpen){
            _readChannel = FileChannel.open(file, READ)
            isOpenForRead = true
        }
        return _readChannel
    }

    private lateinit var _writeChannel: FileChannel
    private val writeChannel: FileChannel
        get(){
            if (isReadOnly) {
                throw IOException("Trying to write to readonly file $file")
            }
            if(!isOpenForWrite || !_writeChannel.isOpen){
                _writeChannel = FileChannel.open(file, WRITE)
                isOpenForWrite = true
            }
            return _writeChannel
        }

    private val readerPos: Long
        get() {
            return readChannel.position()
        }

    private val eofPos: Long
        get() {
            return writeChannel.size()
        }


    private val tag: EnvelopeTag by lazy {
        buildTag().read(readChannel)
    }

    private val _meta: Meta by lazy {
        try {
            readChannel.position(tag.length.toLong())
            val buffer = ByteBuffer.allocate(tag.metaSize)
            readChannel.read(buffer)
            tag.metaType.reader.readBuffer(buffer)
        } catch (e: Exception) {
            throw RuntimeException("Can't read meta from file Envelope", e)
        }
    }

    override//getRandomAccess().seek(getDataOffset());
    val data: FileBinary
        get() {
            try {
                var dataSize = tag.dataSize.toLong()
                if (dataSize <= 0) {
                    dataSize = readChannel.size() - dataOffset
                }
                return FileBinary(file, dataOffset.toInt(), dataSize.toInt())
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }

        }

    private val isEof: Boolean
        get() {
            try {
                return readerPos == eofPos
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }

        }

    /**
     * @return the dataOffset
     */
    private val dataOffset: Long
        get() = (tag.length + tag.metaSize).toLong()

    protected open fun buildTag(): EnvelopeTag {
        return EnvelopeTag()
    }


    @Synchronized
    @Throws(Exception::class)
    override fun close() {
        LoggerFactory.getLogger(javaClass).trace("Closing FileEnvelope $file")
        if (isOpenForRead) {
            readChannel.close()
            isOpenForRead = false
        }
        if (isOpenForWrite) {
            writeChannel.close()
            isOpenForWrite = false
        }
    }

    @Synchronized
    override fun getMeta(): Meta {
        return _meta
    }

    /**
     * Read line starting at given offset
     *
     * @param offset
     * @return
     * @throws IOException
     */
    @Synchronized
    @Throws(IOException::class)
    fun readLine(offset: Int): String {
        //TODO move to binary?
        readChannel.position(dataOffset + offset)
        Channels.newReader(readChannel, "UTF-8").use { stream ->
            val buffer = ByteArrayOutputStream()
            var nextChar = stream.read()
            while (readChannel.position() < readChannel.size() && nextChar != '\r'.toInt()) {
                buffer.write(nextChar)
                nextChar = stream.read()
            }
            return String(buffer.toByteArray(), Charset.forName("UTF-8")).replace("\\n", NEWLINE)
        }
    }


    @Throws(IOException::class)
    private fun readBlock(pos: Int, length: Int): ByteBuffer {
        val block = ByteBuffer.allocate(length)
        readChannel.read(block, pos.toLong())
        return block
    }

    /**
     * Create a new file with the same header, but without data.
     *
     * @throws IOException
     */
    @Synchronized
    @Throws(IOException::class)
    fun clearData() {
        val header = readBlock(0, dataOffset.toInt())
        Files.newByteChannel(file, WRITE, TRUNCATE_EXISTING).use { channel ->
            channel.write(header)
            setDataSize(channel, 0)
        }
    }


    /**
     * Reset file pointer to data start
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun resetPos() {
        readChannel.position(dataOffset)
    }

    @Synchronized
    @Throws(IOException::class)
    private fun setDataSize(channel: SeekableByteChannel, size: Int) {
        tag.setValue(Envelope.DATA_LENGTH_PROPERTY, size)//update property
        val position = channel.position()
        channel.position(0)//seeking begin
        val buffer = tag.toBytes()
        buffer.position(0)
        val d = channel.write(buffer)
        channel.position(position)//return to the initial position
    }

    /**
     * Append byte array to the end of file without escaping and update data
     * size envelope property
     *
     * @param bytes
     * @throws IOException
     */
    @Synchronized
    @Throws(IOException::class)
    fun append(bytes: ByteBuffer) {
        writeChannel.position(eofPos)
        writeChannel.write(bytes)
        setDataSize(writeChannel, (writeChannel.size() - dataOffset).toInt())
    }


    @Synchronized
    @Throws(IOException::class)
    fun append(bytes: ByteArray) {
        this.append(ByteBuffer.wrap(bytes))
    }

    /**
     * Append a new line with escaped new line characters
     *
     * @param line
     * @throws IOException
     */
    @Throws(IOException::class)
    fun appendLine(line: String) {
        append((line.replace("\n", "\\n") + NEWLINE).toByteArray())
    }


    override fun hasData(): Boolean {
        try {
            return this.eofPos > this.dataOffset
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }

    }

    @Throws(ObjectStreamException::class)
    private fun writeReplace(): Any {
        return SimpleEnvelope(meta, data)
    }

    companion object {
        //val INFINITE_DATA_SIZE = Integer.toUnsignedLong(-1)
        private const val NEWLINE = "\r\n"

        /**
         * Create empty envelope with given meta
         *
         * @param path
         * @param meta
         * @return
         */
        @Deprecated("")
        @Throws(IOException::class)
        fun createEmpty(path: Path, meta: Meta): FileEnvelope {
            Files.newOutputStream(path, CREATE, WRITE).use { stream -> DefaultEnvelopeType.INSTANCE.writer.write(stream, EnvelopeBuilder().setMeta(meta)) }
            return FileEnvelope(path, false)
        }

        fun open(path: Path, readOnly: Boolean): FileEnvelope {
            if (!Files.exists(path)) {
                throw RuntimeException("File envelope does not exist")
            }
            return FileEnvelope(path, readOnly)
        }

        fun open(uri: String, readOnly: Boolean): FileEnvelope {
            return open(Paths.get(URI.create(uri)), readOnly)
        }
    }

}
