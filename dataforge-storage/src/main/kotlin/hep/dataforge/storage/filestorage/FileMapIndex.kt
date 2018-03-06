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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.exceptions.StorageException
import hep.dataforge.storage.commons.MapIndex
import hep.dataforge.values.Value
import hep.dataforge.values.ValueUtils
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import java.util.*

/**
 * @author Alexander Nozik
 */
abstract class FileMapIndex<T>(
        private val context: Context,
        private val file: FileEnvelope) : MapIndex<T, Int>(), ContextAware {

    /**
     * The size of the data when last indexed
     */
    private var indexedSize = 0

    /**
     * The size of the data when last saved
     */
    private var savedSize: Long = -1

    private val indexFileDirectory: Path
        get() = context.io.tmpDir.resolve("storage/fileindex")

    private val indexFile: Path
        @Throws(StorageException::class)
        get() = indexFileDirectory.resolve(indexFileName())

    @Synchronized
    @Throws(StorageException::class)
    override fun update() {
        try {
            if (map.isEmpty()) {
                loadIndex()
            }
            if (!isUpToDate(file)) {
                val buffer = file.data.getBuffer(indexedSize)
                buffer.position(0)
                var linePos = 0
                val baos = ByteArrayOutputStream()
                while (buffer.hasRemaining()) {
                    val next = buffer.get()
                    if (next == '\n'.toByte()) {
                        val line = baos.toString("UTF8")
                        val str = line.trim { it <= ' ' }
                        if (!str.startsWith("#") && !str.isEmpty()) {
                            val entry = readEntry(str)
                            val indexValue = getIndexedValue(entry)
                            putToIndex(indexValue, linePos)
                        }
                        //resetting collection
                        baos.reset()
                        linePos = buffer.position()
                    } else {
                        baos.write(next.toInt())
                    }

                }
                indexedSize = buffer.position()
            }
            if (needsSave()) {
                saveIndex()
            }
        } catch (ex: IOException) {
            throw StorageException(ex)
        }

    }

    protected abstract fun indexFileName(): String

    protected abstract fun readEntry(str: String): T

    protected fun needsSave(): Boolean {
        return indexedSize - savedSize >= 200
    }

    override fun transform(key: Int): T {
        try {
            return readEntry(file.readLine(key))
        } catch (ex: IOException) {
            throw RuntimeException("Can't read entry for key " + key, ex)
        }

    }

    @Throws(IOException::class)
    private fun isUpToDate(env: FileEnvelope): Boolean {
        return env.data.size() == this.indexedSize.toLong()
    }

    @Throws(StorageException::class)
    override fun invalidate() {
        val indexFile = indexFile
        try {
            Files.deleteIfExists(indexFile)
        } catch (e: IOException) {
            logger.error("Failed to reset index file {}", indexFile, e)
        }

        indexedSize = 0
        super.invalidate()
    }

    override fun getContext(): Context {
        return context
    }


    /**
     * Load index content from external file
     */
    @Synchronized
    @Throws(StorageException::class)
    private fun loadIndex() {
        val indexFile = indexFile
        if (Files.exists(indexFile)) {
            LoggerFactory.getLogger(javaClass).info("Loading index from file...")
            try {
                ObjectInputStream(Files.newInputStream(indexFile)).use { ois ->
                    val position = ois.readLong().toInt()
                    val newMap = TreeMap<Value, MutableList<Int>>(ValueUtils.VALUE_COMPARATOR)
                    while (ois.available() > 0) {
                        val value = ValueUtils.readValue(ois)
                        val num = ois.readShort()
                        val integers = ArrayList<Int>()
                        for (i in 0 until num) {
                            integers.add(ois.readInt())
                        }
                        newMap[value] = integers
                    }


                    if (position > 0 && position >= this.indexedSize) {
                        this.map = newMap
                        this.indexedSize = position
                    }
                }
            } catch (ex: IOException) {
                LoggerFactory.getLogger(javaClass).error("Failed to read index file. Removing index file", ex)
                indexFile.toFile().delete()
            } catch (ex: ClassNotFoundException) {
                LoggerFactory.getLogger(javaClass).error("Failed to read index file. Removing index file", ex)
                indexFile.toFile().delete()
            }

        } else {
            LoggerFactory.getLogger(javaClass).debug("Index file not found")
        }
    }

    /**
     * Save index to default file
     *
     * @throws StorageException
     */
    @Synchronized
    @Throws(StorageException::class)
    private fun saveIndex() {
        val indexFile = indexFile
        try {
            LoggerFactory.getLogger(javaClass).info("Saving index to file...")
            if (!Files.exists(indexFile.parent)) {
                Files.createDirectories(indexFile.parent)
            }
            ObjectOutputStream(Files.newOutputStream(indexFile, WRITE, CREATE, TRUNCATE_EXISTING)).use { ous ->
                ous.writeLong(indexedSize.toLong())
                map.forEach { value, integers ->
                    try {
                        ValueUtils.writeValue(ous, value)
                        ous.writeShort(integers.size)
                        for (i in integers) {
                            ous.writeInt(i)
                        }
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
            }
            savedSize = indexedSize.toLong()
        } catch (ex: IOException) {
            LoggerFactory.getLogger(javaClass).error("Failed to write index file. Removing index file.", ex)
        }

    }

}
