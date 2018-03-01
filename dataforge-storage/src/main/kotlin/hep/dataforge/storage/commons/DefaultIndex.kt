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
import hep.dataforge.values.ValueUtils
import javafx.util.Pair
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * The simple index, which uses item number for search
 *
 * @param <T>
 * @author Alexander Nozik
</T> */
class DefaultIndex<T>(private val iterable: Iterable<T>) : ValueIndex<T> {

    fun stream(): Stream<Pair<Int, T>> {
        val counter = AtomicInteger(0)
        return StreamSupport.stream(iterable.spliterator(), false).map { it -> Pair(counter.getAndIncrement(), it) }
    }

    @Throws(StorageException::class)
    override fun pull(value: Value): Stream<T> {
        return stream().filter { pair -> value.intValue() == pair.key }
                .map{ it.value }
    }

    @Throws(StorageException::class)
    override fun pull(from: Value, to: Value): Stream<T> {
        return stream().filter { pair -> ValueUtils.isBetween(pair.key, from, to) }
                .map{ it.value }
    }

    fun pull(predicate: Predicate<Int>): Stream<T> {
        return stream().filter { t -> predicate.test(t.key) }
                .map{ it.value }
    }

    override fun keySet(): NavigableSet<Value> {
        val res = TreeSet<Value>()
        stream().forEach { it -> res.add(Value.of(it.key)) }
        return res
    }
}
