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
package hep.dataforge.storage.commons

import hep.dataforge.exceptions.StorageException
import hep.dataforge.storage.api.ValueIndex
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Simple in memory index. Very inefficient.
 *
 * @param <T>
 * @author Alexander Nozik
</T> */
class ValueProviderIndex<T : ValueProvider> : ValueIndex<T> {

    private val valueName: String
    private val defaultValue: Value
    private val iterable: Iterable<T>

    constructor(iterable: Iterable<T>, valueName: String) {
        this.iterable = iterable
        this.valueName = valueName
        defaultValue = Value.NULL
    }

    /**
     * @param iterable
     * @param valueName
     * @param defaultValue the default value in case some of iterated items does
     * not provide required name
     */
    constructor(iterable: Iterable<T>, valueName: String, defaultValue: Value) {
        this.iterable = iterable
        this.valueName = valueName
        this.defaultValue = defaultValue
    }

    @Throws(StorageException::class)
    override fun pull(value: Value): Stream<T> {
        return StreamSupport.stream(iterable.spliterator(), true)
                .filter { it -> it.getValue(valueName, defaultValue) == value }
    }

    @Throws(StorageException::class)
    override fun pull(from: Value, to: Value): Stream<T> {
        return StreamSupport.stream(iterable.spliterator(), true)
                .filter { it -> it.getValue(valueName, defaultValue) in from..to }
    }

    override fun keySet(): NavigableSet<Value> {
        val res = TreeSet<Value>()
        StreamSupport.stream(iterable.spliterator(), true).map { it -> it.getValue(valueName, defaultValue) }.forEach { it -> res.add(it) }
        return res
    }
}
