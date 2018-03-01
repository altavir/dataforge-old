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
import hep.dataforge.exceptions.StorageException
import hep.dataforge.io.IOUtils
import hep.dataforge.io.LineIterator
import hep.dataforge.meta.Meta
import hep.dataforge.names.Names
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.api.ValueIndex
import hep.dataforge.storage.commons.DefaultIndex
import hep.dataforge.storage.loaders.AbstractTableLoader
import hep.dataforge.tables.MetaTableFormat
import hep.dataforge.tables.PointParser
import hep.dataforge.tables.SimpleParser
import hep.dataforge.tables.TableFormat
import hep.dataforge.values.Value
import hep.dataforge.values.Values
import org.apache.commons.io.FilenameUtils

import java.io.BufferedReader
import java.io.IOException
import java.nio.channels.Channels
import java.nio.file.Path
import java.util.function.Supplier

/**
 * @author Alexander Nozik
 */
class FileTableLoader(storage: Storage, name: String, meta: Meta, private val path: Path) : AbstractTableLoader(storage, name, meta) {
    //FIXME move to abstract
    private var format: TableFormat? = null
    private var parser: PointParser? = null

    /**
     * An envelope used for pushing
     */
    private var envelope: FileEnvelope? = null

    override val isEmpty: Boolean
        get() {
            try {
                return envelope != null && !envelope!!.hasData() || !buildEnvelope(true).hasData()
            } catch (ex: StorageException) {
                throw RuntimeException("Can't access loader envelope", ex)
            }

        }

    @Throws(Exception::class)
    override fun open() {
        if (this.meta == null) {
            this.meta = buildEnvelope(true).meta
        }
        // read format from first line if it is not defined in meta
        if (getFormat() == null) {
            buildEnvelope(true).use { envelope ->
                BufferedReader(Channels.newReader(envelope.data.channel, "UTF8"))
                        .lines()
                        .findFirst()
                        .ifPresent { line ->
                            if (line.startsWith("#f")) {
                                format = MetaTableFormat.forNames(Names.of(*line.substring(2).trim { it <= ' ' }.split("[^\\w']+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
                            }
                        }
            }
        }
    }

    @Throws(Exception::class)
    override fun close() {
        parser = null
        format = null
        if (envelope != null) {
            envelope!!.close()
            envelope = null
        }
        super.close()
    }

    private fun buildEnvelope(readOnly: Boolean): FileEnvelope {
        return FileEnvelope.open(path, readOnly)
    }

    /**
     * Get writeable reusable single access envelope for this loader
     *
     * @return
     */
    private fun getEnvelope(): FileEnvelope {
        if (this.envelope == null) {
            this.envelope = buildEnvelope(false)
        }
        return this.envelope
    }

    override fun getFormat(): TableFormat? {
        if (format == null) {
            if (getMeta()!!.hasMeta("format")) {
                format = MetaTableFormat(getMeta()!!.getMeta("format"))
            } else if (getMeta()!!.hasValue("format")) {
                format = MetaTableFormat.forNames(getMeta()!!.getStringArray("format"))
            } else {
                format = null
            }
        }
        return format
    }

    private fun getParser(): PointParser {
        if (parser == null) {
            parser = SimpleParser(getFormat()!!)
        }
        return parser
    }

    @Throws(StorageException::class)
    override fun pushPoint(dp: Values) {
        try {
            if (!getEnvelope().hasData()) {
                getEnvelope().appendLine(IOUtils.formatCaption(getFormat()!!))
            }
            val str = IOUtils.formatDataPoint(getFormat()!!, dp)
            getEnvelope().appendLine(str)
        } catch (ex: IOException) {
            throw StorageException("Error while openning an envelope", ex)
        }

    }

    override fun iterator(): Iterator<Values> {
        try {
            val reader = buildEnvelope(true)
            val iterator = LineIterator(reader.data.stream, "UTF-8")
            return object : Iterator<Values> {
                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }

                override fun next(): Values {
                    return transform(iterator.next())
                }
            }
        } catch (ex: StorageException) {
            throw RuntimeException(ex)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }

    }

    private fun transform(line: String?): Values {
        return getParser().parse(line)
    }

    public override fun buildIndex(name: String?): ValueIndex<Values> {
        return if (name == null || name.isEmpty()) {
            //use point number index
            DefaultIndex(this)
        } else {
            FilePointIndex(name, storage.context, Supplier { this.getEnvelope() })
        }
    }


    private inner class FilePointIndex(private val valueName: String, context: Context, sup: Supplier<FileEnvelope>) : FileMapIndex<Values>(context, sup) {

        override fun getIndexedValue(entry: Values): Value {
            return entry.getValue(valueName)
        }

        override fun indexFileName(): String {
            return if (storage.isAnonimous) {
                name + "_" + valueName
            } else {
                storage.name + "/" + name + "_" + valueName
            }
        }

        override fun readEntry(str: String): Values {
            return this@FileTableLoader.transform(str)
        }

    }

    companion object {

        @Throws(Exception::class)
        fun fromEnvelope(storage: Storage, envelope: FileEnvelope): FileTableLoader {
            if (FileStorageEnvelopeType.validate(envelope, TableLoader.TABLE_LOADER_TYPE)) {
                val res = FileTableLoader(storage,
                        FilenameUtils.getBaseName(envelope.file.fileName.toString()),
                        envelope.meta,
                        envelope.file)
                res.isReadOnly = envelope.isReadOnly
                return res
            } else {
                throw StorageException("Is not a valid point loader file")
            }
        }
    }
}
