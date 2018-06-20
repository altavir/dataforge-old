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

import hep.dataforge.exceptions.StorageException
import hep.dataforge.io.IOUtils
import hep.dataforge.io.LineIterator
import hep.dataforge.isAnonymous
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.api.TableLoader
import hep.dataforge.storage.api.ValueIndex
import hep.dataforge.storage.commons.DefaultIndex
import hep.dataforge.tables.*
import hep.dataforge.values.Value
import hep.dataforge.values.Values
import java.io.IOException
import java.util.*

/**
 * @author Alexander Nozik
 */
class FileTableLoader(storage: Storage, name: String, meta: Meta, file: FileEnvelope) : FileLoader(storage, name, meta, file), TableLoader {

    override val format: TableFormat by lazy {
        when {
            file.meta.hasMeta("format") -> MetaTableFormat(meta.getMeta("format"))
            file.meta.hasValue("format") -> MetaTableFormat.forNames(*meta.getStringArray("format"))
            else -> throw RuntimeException("Format definition not found")
        }
    }

    private val parser: ValuesParser by lazy {
        SimpleValuesParser(format)
    }

    override fun iterator(): MutableIterator<Values> {
        try {
            val iterator = LineIterator(file.data.stream, "UTF-8")
            return object : MutableIterator<Values> {

                override fun remove() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }

                override fun next(): Values {
                    return parser.parse(iterator.next())
                }
            }
        } catch (ex: StorageException) {
            throw RuntimeException(ex)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }

    }

    private fun buildIndex(name: String?): ValueIndex<Values> {
        return if (name == null || name.isEmpty()) {
            //use point number index
            DefaultIndex(this)
        } else {
            FilePointIndex(name)
        }
    }


    private val indexMap = HashMap<String, ValueIndex<Values>>()

    @Throws(StorageException::class)
    override fun push(dps: Collection<Values>) {
        for (dp in dps) {
            push(dp)
        }
    }

    @Synchronized
    override fun getIndex(name: String): ValueIndex<Values> {
        return indexMap.computeIfAbsent(name) { this.buildIndex(it) }
    }

    /**
     * Push point and notify all listeners
     *
     * @param dp
     * @throws StorageException
     */
    @Throws(StorageException::class)
    override fun push(dp: Values) {
        //Notifying the listener
        connectionHelper.forEachConnection(ValuesListener::class.java) {
            it.accept(dp)
        }

        try {
            val str = IOUtils.formatDataPoint(format, dp)
            file.appendLine(str)
        } catch (ex: IOException) {
            throw StorageException("Error while opening an envelope", ex)
        }
    }

//    override fun respond(message: Envelope): Envelope {
//        try {
//            if (!validator.isValid(message)) {
//                return StorageMessageUtils.exceptionResponse(message, WrongTargetException())
//            }
//            val messageMeta = message.meta
//            val operation = messageMeta.getString(ACTION_KEY)
//            when (operation) {
//                PUSH_ACTION -> {
//                    if (!messageMeta.hasMeta("data")) {
//                        //TODO реализовать бинарную передачу данных
//                        throw StorageException("No data in the push data command")
//                    }
//
//                    val data = messageMeta.getMeta("data")
//                    for (dp in ListOfPoints.buildFromMeta(data)) {
//                        this.push(dp)
//                    }
//
//                    return confirmationResponse(message)
//                }
//
//                PULL_ACTION -> {
//                    var points: List<Values> = ArrayList()
//                    when {
//                        messageMeta.hasMeta(QUERY_ELEMENT) -> points = index.query(messageMeta.getMeta(QUERY_ELEMENT)).toList()
//                        messageMeta.hasValue("value") -> {
//                            val valueName = messageMeta.getString("valueName", "")
//                            points = messageMeta.getValue("value").list.stream()
//                                    .map { `val` -> getIndex(valueName).pullOne(`val`) }
//                                    .filter { it.isPresent }.map<Values> { it.get() }
//                                    .toList()
//                        }
//                        messageMeta.hasMeta("range") -> {
//                            val valueName = messageMeta.getString("valueName", "")
//                            for (rangeAn in messageMeta.getMetaList("range")) {
//                                val from = rangeAn.getValue("from", Value.NULL)
//                                val to = rangeAn.getValue("to", Value.NULL)
//                                //                            int maxItems = rangeAn.getInt("maxItems", Integer.MAX_VALUE);
//                                points = this.getIndex(valueName).pull(from, to).toList()
//                            }
//                        }
//                    }
//
//                    val dataAn = MetaBuilder("data")
//                    for (dp in points) {
//                        dataAn.putNode(dp.toMeta())
//                    }
//                    return okResponseBase(message, true, false)
//                            .putMetaNode(dataAn)
//                            .setMetaValue("data.size", points.size)
//                            .build()
//                }
//
//                else -> throw NotDefinedException(operation)
//            }
//
//        } catch (ex: StorageException) {
//            return StorageMessageUtils.exceptionResponse(message, ex)
//        } catch (ex: UnsupportedOperationException) {
//            return StorageMessageUtils.exceptionResponse(message, ex)
//        } catch (ex: NotDefinedException) {
//            return StorageMessageUtils.exceptionResponse(message, ex)
//        }
//
//    }


    private inner class FilePointIndex(private val valueName: String) : FileMapIndex<Values>(context, file) {

        override fun getIndexedValue(entry: Values): Value {
            return entry.getValue(valueName)
        }

        override fun indexFileName(): String {
            return if (storage.isAnonymous) {
                name + "_" + valueName
            } else {
                storage.name + "/" + name + "_" + valueName
            }
        }

        override fun readEntry(str: String): Values {
            return parser.parse(str)
        }

    }
}
