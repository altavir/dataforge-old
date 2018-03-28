/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary

import java.io.IOException
import java.io.InputStream
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel

/**
 * An interface to represent something that one can read binary data from in a
 * blocking or non-blocking way. This interface is intended for read access
 * only.
 *
 * @author Alexander Nozik
 */
interface Binary : Serializable {


    /**
     * Get blocking input stream for this binary
     *
     * @return
     */
    val stream: InputStream

    /**
     * Get non-blocking byte channel
     *
     * @return
     */
    val channel: ReadableByteChannel

    /**
     * Read the content of this binary to a single byte buffer.
     *
     * @return
     * @throws IOException
     */
    val buffer: ByteBuffer
        get() {
            if (size() >= 0) {
                val buffer = ByteBuffer.allocate(size().toInt())
                channel.read(buffer)
                return buffer
            } else {
                throw IOException("Can not convert binary of undefined size to buffer")
            }
        }

    /**
     * The size of this binary. Negative value corresponds to undefined size.
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun size(): Long

    companion object {
        //TODO add randomAcessBinary?

        val EMPTY: Binary = BufferedBinary(ByteArray(0))
    }
}