/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary

import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.READ
import java.util.stream.Stream

class FileBinary : Binary {

    /**
     * File to create binary from
     */
    private val file: Path

    /**
     * dataOffset form beginning of file
     */
    private val dataOffset: Int

    private val size: Int

    override val stream: InputStream
        @Throws(IOException::class)
        get() = getStream(0)

    override val channel: ByteChannel
        @Throws(IOException::class)
        get() = Files.newByteChannel(file, READ).position(dataOffset.toLong())

    override val buffer: ByteBuffer
        @Throws(IOException::class)
        get() = getBuffer(0, size().toInt())

    constructor(file: Path, dataOffset: Int, size: Int) {
        this.file = file
        this.dataOffset = dataOffset
        this.size = size
    }

    constructor(file: Path, dataOffset: Int) {
        this.file = file
        this.dataOffset = dataOffset
        this.size = -1
    }

    constructor(file: Path) {
        this.file = file
        this.dataOffset = 0
        this.size = -1
    }

    @Throws(IOException::class)
    fun getStream(offset: Int): InputStream {
        val stream = Files.newInputStream(file, READ)
        stream.skip((dataOffset + offset).toLong())
        return stream
    }

    /**
     * Read a buffer with given dataOffset in respect to data block start and given size. If data size w
     *
     * @param offset
     * @param size
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getBuffer(offset: Int, size: Int): ByteBuffer {
        FileChannel.open(file).use { channel ->
            val buffer = ByteBuffer.allocate(size)
            channel.read(buffer, (offset + dataOffset).toLong())
            return buffer
        }
    }

    @Throws(IOException::class)
    fun getBuffer(start: Int): ByteBuffer {
        return getBuffer(start, (size() - start).toInt())
    }

    @Throws(IOException::class)
    override fun size(): Long {
        return if (size >= 0) size.toLong() else Files.size(file) - dataOffset
    }

    @Throws(IOException::class)
    fun lines(): Stream<String> {
        return BufferedReader(InputStreamReader(stream)).lines()
    }


    @Throws(ObjectStreamException::class)
    private fun writeReplace(): Any {
        try {
            return BufferedBinary(buffer.array())
        } catch (e: IOException) {
            throw WriteAbortedException("Failed to get byte buffer", e)
        }

    }

}
