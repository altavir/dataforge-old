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
package hep.dataforge.storage.api

import hep.dataforge.description.NodeDef
import hep.dataforge.exceptions.StorageException
import hep.dataforge.tables.TableFormat
import hep.dataforge.tables.ValuesSource
import hep.dataforge.values.Values

/**
 * PointLoader is intended to load a set of datapoints. The loader can have one
 * index field by which it could be sorted and searched. If index field is not
 * defined, than default internal indexing mechanism is used.
 *
 * @author Darksnake
 */
@NodeDef(key = "format", required = true, info = "data point format for this loader")
//@ValueDef(name = "defaultIndexName", def = "timestamp", info = "The name of index field for this loader")
interface TableLoader : Loader, ValuesSource {

    override val type: String
        get() = TABLE_LOADER_TYPE
    /**
     * The minimal format for points in this loader. Is null for unformatted loader
     *
     * @return
     */
    val format: TableFormat

    /**
     * get default index
     *
     * @return
     */
    val index: ValueIndex<Values>
        get() = when {
            meta.hasValue("index") -> getIndex(meta.getString("index"))
            meta.hasValue("index.key") -> getIndex(meta.getString("index.key"))
            else -> getIndex(DEFAULT_INDEX_FIELD)
        }

    /**
     * Get index for given value name. If name is null or empty, default point
     * number index is returned. This operation chooses the fastest existing
     * index or creates new one (if index is created than it is optimized for
     * single operation performance).
     *
     * @param name
     * @return
     */
    fun getIndex(name: String): ValueIndex<Values>

    /**
     * Push the DataPoint to the loader.
     *
     * @param dp
     * @throws StorageException in case push failed
     */
    @Throws(StorageException::class)
    fun push(dp: Values)

    /**
     * Push a collection of DataPoints. This method should be overridden when
     * Loader commit operation is expensive and should be used once for the
     * whole collection.
     *
     * @param dps
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun push(dps: Collection<Values>){
        dps.forEach { push(it) }
    }

    companion object {

        const val TABLE_LOADER_TYPE = "loader.table"
        const val DEFAULT_INDEX_FIELD = ""
    }

}
