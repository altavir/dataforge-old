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

package hep.dataforge.storage

import hep.dataforge.Type
import hep.dataforge.meta.Meta
import hep.dataforge.tables.ListTable
import hep.dataforge.tables.Table
import hep.dataforge.tables.TableFormat
import hep.dataforge.tables.ValuesSource
import hep.dataforge.values.Value
import hep.dataforge.values.Values

@Type("hep.dataforge.storage.loader.table")
interface TableLoader : Loader<Values>, ValuesSource {
    /**
     * Format of the table
     */
    val format: TableFormat

    /**
     * Generate indexed loader based on this one. Type of the indesx is defined by meta
     */
    fun indexed(meta: Meta = Meta.empty()): IndexedTableLoader

    /**
     * Generate a mutable loader based on this one. Throws an exception if it is not possible
     */
    fun mutable(): MutableTableLoader

    @JvmDefault
    suspend fun asTable(): Table {
        // Replace with custom table which will be updated with the loader
        return ListTable(format, this.toList())
    }
}

interface IndexedTableLoader : TableLoader, IndexedLoader<Value, Values> {
    suspend fun get(any: Any): Values? = get(Value.of(any))

    /**
     * Notify loader that it should update index for this loader
     */
    fun updateIndex()
}

/**
 * Select a range from this table loade
 */
suspend fun IndexedTableLoader.select(from: Value, to: Value): Table {
    return ListTable(format, keys.subSet(from, true, to, true).map { get(it)!! })
}

suspend fun IndexedTableLoader.select(query: Meta): Table {
    TODO("To be implemented")
}


interface MutableTableLoader : TableLoader, AppendableLoader<Values>

