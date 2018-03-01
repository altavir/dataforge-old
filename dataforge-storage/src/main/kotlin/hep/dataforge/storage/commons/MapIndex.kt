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

import java.util.*
import java.util.stream.Stream
import kotlin.collections.ArrayList

/**
 * A fast access index based on treeMap. It needs to be updated prior to any use
 * and requires a lot of memory, so it should be used only in cases of repeated
 * pull requests.
 *
 * @param <K> intermediate key representation for entries.
 * @author Alexander Nozik
</K> */
abstract class MapIndex<T, K> : ValueIndex<T> {

    //TODO add custom request that fetches roots of the tree
    protected var map = TreeMap<Value, MutableList<K>>(ValueUtils.VALUE_COMPARATPR)

    /**
     * Store index entry
     *
     * @param v
     * @param key
     */
    protected fun putToIndex(v: Value, key: K) {
        map.getOrPut(v){
            ArrayList()
        }.add(key)
    }

    /**
     * Get stored value by key. Could use some external information.
     *
     * @param key
     * @return
     */
    protected abstract fun transform(key: K): T

    protected abstract fun getIndexedValue(entry: T): Value

    /**
     * Update index to match source
     */
    @Throws(StorageException::class)
    protected abstract fun update()

    //    private List<Supplier<T>> transform(List<K> list) {
    //        if (list == null) {
    //            return Collections.emptyList();
    //        }
    //        return list.stream().<Supplier<T>>map(k -> () -> transform(k)).collect(Collectors.toList());
    //    }

    @Throws(StorageException::class)
    override fun pull(value: Value): Stream<T> {
        update()
        val list = map[value]
        return if (list == null) {
            Stream.empty()
        } else {
            list.stream().map { this.transform(it) }
        }
    }

    @Throws(StorageException::class)
    override fun pullOne(value: Value): Optional<T> {
        val entry = map.ceilingEntry(value)
        return if (entry != null) {
            entry.value.stream().map<T> { this.transform(it) }.findFirst()
        } else {
            Optional.empty()
        }
    }

    @Throws(StorageException::class)
    override fun pull(from: Value, to: Value): Stream<T> {
        var from = from
        var to = to
        update()
        if (map.isEmpty()) {
            return Stream.empty()
        }
        //If null, use the whole range
        if (from === Value.NULL) {
            from = map.firstKey()
        }

        if (to === Value.NULL) {
            to = map.lastKey()
        }


        return map.subMap(from, true, to, true).values.stream().flatMap { u -> u.stream().map<T> { this.transform(it) } }
    }


    @Throws(StorageException::class)
    open fun invalidate() {
        this.map.clear()
    }

    @Throws(StorageException::class)
    override fun keySet(): NavigableSet<Value> {
        update()
        return this.map.navigableKeySet()
    }
}
