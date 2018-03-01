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
package hep.dataforge.storage.loaders

import hep.dataforge.exceptions.NotDefinedException
import hep.dataforge.exceptions.StorageException
import hep.dataforge.exceptions.WrongTargetException
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.api.TableLoader
import hep.dataforge.storage.api.ValueIndex
import hep.dataforge.storage.commons.MessageFactory
import hep.dataforge.storage.commons.StorageMessageUtils
import hep.dataforge.tables.ListOfPoints
import hep.dataforge.tables.PointListener
import hep.dataforge.values.Value
import hep.dataforge.values.Values
import java.util.*
import java.util.stream.Collectors

/**
 * @author Alexander Nozik
 */
abstract class AbstractTableLoader(storage: Storage, name: String, meta: Meta) : AbstractLoader(storage, name, meta), TableLoader {

    protected val listeners: MutableSet<PointListener> = HashSet()
    private val indexMap = HashMap<String, ValueIndex<Values>>()

    override val type: String
        get() = TableLoader.TABLE_LOADER_TYPE

    @Throws(StorageException::class)
    override fun push(dps: Collection<Values>) {
        for (dp in dps) {
            push(dp)
        }
    }

    @Synchronized
    override fun getIndex(name: String): ValueIndex<Values> {
        return (indexMap as java.util.Map<String, ValueIndex<Values>>).computeIfAbsent(name, Function<String, ValueIndex<Values>> { this.buildIndex(it) })
    }

    protected abstract fun buildIndex(name: String): ValueIndex<Values>

    /**
     * Push point and notify all listeners
     *
     * @param dp
     * @throws StorageException
     */
    @Throws(StorageException::class)
    override fun push(dp: Values) {
        //Notifying the listener
        listeners.forEach { l -> l.accept(dp) }
        pushPoint(dp)
    }

    /**
     * push procedure implementation
     *
     * @param dp
     * @throws StorageException
     */
    @Throws(StorageException::class)
    protected abstract fun pushPoint(dp: Values)

    override fun respond(message: Envelope): Envelope {
        try {
            if (!validator.isValid(message)) {
                return StorageMessageUtils.exceptionResponse(message, WrongTargetException())
            }
            val messageMeta = message.meta
            val operation = messageMeta.getString(ACTION_KEY)
            when (operation) {
                PUSH_OPERATION -> {
                    if (!messageMeta.hasMeta("data")) {
                        //TODO реализовать бинарную передачу данных
                        throw StorageException("No data in the push data command")
                    }

                    val data = messageMeta.getMeta("data")
                    for (dp in ListOfPoints.buildFromMeta(data)) {
                        this.push(dp)
                    }

                    return confirmationResponse(message)
                }

                PULL_OPERATION -> {
                    var points: List<Values> = ArrayList()
                    if (messageMeta.hasMeta(QUERY_ELEMENT)) {
                        points = index.query(messageMeta.getMeta(QUERY_ELEMENT)).collect<List<Values>, Any>(Collectors.toList())
                    } else if (messageMeta.hasValue("value")) {
                        val valueName = messageMeta.getString("valueName", "")
                        points = messageMeta.getValue("value").listValue().stream()
                                .map { `val` -> getIndex(valueName).pullOne(`val`) }
                                .filter(Predicate<Optional<Values>> { it.isPresent() }).map<Values>(Function<Optional<Values>, Values> { it.get() })
                                .collect<List<Values>, Any>(Collectors.toList())
                    } else if (messageMeta.hasMeta("range")) {
                        val valueName = messageMeta.getString("valueName", "")
                        for (rangeAn in messageMeta.getMetaList("range")) {
                            val from = rangeAn.getValue("from", Value.getNull())
                            val to = rangeAn.getValue("to", Value.getNull())
                            //                            int maxItems = rangeAn.getInt("maxItems", Integer.MAX_VALUE);
                            points = this.getIndex(valueName).pull(from, to)
                                    .collect<List<Values>, Any>(Collectors.toList())
                        }
                    }

                    val dataAn = MetaBuilder("data")
                    for (dp in points) {
                        dataAn.putNode(dp.toMeta())
                    }
                    return MessageFactory().okResponseBase(message, true, false)
                            .putMetaNode(dataAn)
                            .putMetaValue("data.size", points.size)
                            .build()
                }

                else -> throw NotDefinedException(operation)
            }

        } catch (ex: StorageException) {
            return StorageMessageUtils.exceptionResponse(message, ex)
        } catch (ex: UnsupportedOperationException) {
            return StorageMessageUtils.exceptionResponse(message, ex)
        } catch (ex: NotDefinedException) {
            return StorageMessageUtils.exceptionResponse(message, ex)
        }

    }

    override fun addPointListener(listener: PointListener) {
        this.listeners.add(listener)
    }

    override fun removePointListener(listener: PointListener) {
        this.listeners.remove(listener)
    }

}
