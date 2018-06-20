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
package hep.dataforge.storage.api

import hep.dataforge.exceptions.StorageException
import hep.dataforge.meta.Meta
import hep.dataforge.storage.commons.StorageUtils
import hep.dataforge.values.Value
import java.util.*
import java.util.stream.Stream

/**
 * An index that uses a Value corresponding to each indexed element
 *
 * @author Alexander Nozik
 */
interface ValueIndex<T> : Index<T> {

    val firstKey: Value
        @Throws(StorageException::class)
        get() = keySet().first()

    val lastKey: Value
        @Throws(StorageException::class)
        get() = keySet().last()

    /**
     * Search for the index field value closest to provided one. Specific search
     * mechanism could differ for different indexes.
     *
     * @param value
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    @Throws(StorageException::class)
    fun pull(value: Value): Stream<T>

    @Throws(StorageException::class)
    fun pull(value: Any): Stream<T> {
        return pull(Value.of(value))
    }

    /**
     * Pull the first entry with given key
     *
     * @param value
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    open fun pullOne(value: Value): Optional<T> {
        return pull(value).findFirst()
    }

    @Throws(StorageException::class)
    fun pullOne(value: Any): Optional<T> {
        return pullOne(Value.of(value))
    }

    /**
     * Возвращает список точек, ключ которых лежит строго в пределах от from до
     * to. Работает только для сравнимых значений (для строк может выдавать
     * ерунду)
     *
     * @param from
     * @param to
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    @Throws(StorageException::class)
    fun pull(from: Value, to: Value): Stream<T>

    @Throws(StorageException::class)
    fun pull(from: Any, to: Any): Stream<T> {
        return pull(Value.of(from), Value.of(to))
    }

    /**
     * A sparse pull operation with limited number of results.
     * This method does not guarantee specific node placement but tries to place them as uniformly as possible.
     * It is intended primarily for visualization.
     *
     * @param from
     * @param to
     * @param limit
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun pull(from: Value, to: Value, limit: Int): Stream<T> {
        return StorageUtils.sparsePull(this, from, to, limit)
    }

    /**
     * By default uses smart optimized index pull
     *
     * @param query
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    override fun query(query: Meta): Stream<T> {
        //TODO add support for query engines
        //null values correspond to
        val from = query.getValue(FROM_KEY, Value.NULL)
        val to = query.getValue(TO_KEY, Value.NULL)
        return if (query.hasValue(LIMIT_KEY)) {
            pull(from, to, query.getInt(LIMIT_KEY))
        } else {
            pull(from, to)
        }
    }

    @Throws(StorageException::class)
    fun keySet(): NavigableSet<Value>

    companion object {

        const val FROM_KEY = "from"
        const val TO_KEY = "to"
        const val LIMIT_KEY = "limit"
    }


}
