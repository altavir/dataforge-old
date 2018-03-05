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
package hep.dataforge.storage.commons

import hep.dataforge.exceptions.StorageException
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.storage.api.*
import hep.dataforge.tables.TableFormat
import java.util.*

/**
 * @author darksnake
 */
object LoaderFactory {

    fun buildTableLoaderMeta(indexField: String?, format: TableFormat?): MetaBuilder {
        val builder = MetaBuilder("loader")

        //        if (name == null || name.isEmpty()) {
        //            throw new RuntimeException("The name can not be empty");
        //        } else {
        //            builder.setValue(Loader.LOADER_NAME_KEY, name);
        //        }

        if (indexField != null) {
            builder.putValue("index", indexField)
        }

        builder.putValue(Loader.LOADER_TYPE_KEY, TableLoader.TABLE_LOADER_TYPE)

        if (format != null) {
            builder.putNode(format.toMeta())
            if (Arrays.binarySearch(format.namesAsArray(), "timestamp") > 0) {
                builder.putValue("dynamic", true)
            }
        }

        return builder
    }

    private fun findShelf(storage: Storage, shelfName: String?): Storage {
        return if (shelfName != null && !shelfName.isEmpty()) {
            storage.optShelf(shelfName).orElseGet { storage.buildShelf(shelfName, Meta.empty()) }
        } else {
            storage
        }
    }

    /**
     * A helper to create specific point loader in the storage
     *
     * @param storage
     * @param loaderName
     * @param shelfName
     * @param indexField
     * @param format
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    @JvmStatic
    fun buildPointLoader(storage: Storage, loaderName: String, shelfName: String, indexField: String, format: TableFormat): TableLoader {
        return findShelf(storage, shelfName).buildLoader(loaderName, buildTableLoaderMeta(indexField, format)) as TableLoader
    }

    /**
     * A helper to create specific loader in the storage
     *
     * @param storage
     * @param loaderName
     * @param shelfName
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    @JvmStatic
    fun buildObjectLoder(storage: Storage, loaderName: String, shelfName: String): ObjectLoader<*>? {
        val loaderAn = MetaBuilder("loader")
                .putValue(Loader.LOADER_TYPE_KEY, ObjectLoader.OBJECT_LOADER_TYPE)
                .build()

        return findShelf(storage, shelfName).buildLoader(loaderName, loaderAn) as ObjectLoader<*>
    }

    /**
     * A helper to create specific loader in the storage
     *
     * @param storage
     * @param loaderName
     * @param shelfName
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    @JvmStatic
    fun buildStateLoder(storage: Storage, loaderName: String, shelfName: String): StateLoader {
        val loaderAn = MetaBuilder("loader")
                .putValue(Loader.LOADER_TYPE_KEY, StateLoader.STATE_LOADER_TYPE)
                .build()

        return findShelf(storage, shelfName).buildLoader(loaderName, loaderAn) as StateLoader
    }

    /**
     * A helper to create specific loader in the storage
     *
     * @param storage
     * @param loaderName
     * @param shelfName
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    @JvmStatic
    fun buildEventLoder(storage: Storage, loaderName: String, shelfName: String): EventLoader {
        val loaderAn = MetaBuilder("loader")
                .putValue(Loader.LOADER_TYPE_KEY, EventLoader.EVENT_LOADER_TYPE)
                .build()

        return findShelf(storage, shelfName).buildLoader(loaderName, loaderAn) as EventLoader
    }

    //    public static PointLoader getPointLoader(Storage storage, String name) throws StorageException {
    //        if (storage.hasLoader(name)) {
    //            Loader loader = storage.getLoader(name);
    //            if (loader instanceof PointLoader) {
    //                return (PointLoader) loader;
    //            } else {
    //                throw new LoaderNotFoundException();
    //                //return new MaskPointLoader(loader);
    //            }
    //        } else {
    //            throw new LoaderNotFoundException();
    //        }
    //    }
}
