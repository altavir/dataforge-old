/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.*;
import hep.dataforge.tables.TableFormat;

import java.util.Arrays;

/**
 *
 * @author darksnake
 */
public class LoaderFactory {

    public static MetaBuilder buildDataPointLoaderMeta(String name, String indexField, TableFormat format) {
        MetaBuilder builder = new MetaBuilder("loader");

        if (name == null || name.isEmpty()) {
            throw new RuntimeException("The name can not be empty");
        } else {
            builder.putValue(Loader.LOADER_NAME_KEY, name);
        }

        if (indexField != null) {
            builder.putValue("index", indexField);
        }

        builder.putValue(Loader.LOADER_TYPE_KEY, PointLoader.POINT_LOADER_TYPE);

        if (format != null) {
            builder.putNode(format.toMeta());
            if (Arrays.binarySearch(format.namesAsArray(), "timestamp") > 0) {
                builder.putValue("dynamic", true);
            }
        }

        return builder;
    }

    private static Storage findShelf(Storage storage, String shelfName){
        if (shelfName != null && !shelfName.isEmpty()) {
            return storage.optShelf(shelfName).orElseGet(()-> storage.buildShelf(shelfName, null));
        } else{
            return storage;
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
    public static PointLoader buildPointLoder(Storage storage, String loaderName, String shelfName, String indexField, TableFormat format)
            throws StorageException {
        return (PointLoader) findShelf(storage,shelfName).buildLoader(buildDataPointLoaderMeta(loaderName, indexField, format));
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
    public static <T> ObjectLoader<T> buildObjectLoder(Storage storage, String loaderName, String shelfName)
            throws StorageException {
        Meta loaderAn = new MetaBuilder("loader")
                .putValue(Loader.LOADER_NAME_KEY, loaderName)
                .putValue(Loader.LOADER_TYPE_KEY, ObjectLoader.OBJECT_LOADER_TYPE)
                .build();

        return (ObjectLoader<T>) findShelf(storage,shelfName).buildLoader(loaderAn);
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
    public static StateLoader buildStateLoder(Storage storage, String loaderName, String shelfName)
            throws StorageException {
        Meta loaderAn = new MetaBuilder("loader")
                .putValue(Loader.LOADER_NAME_KEY, loaderName)
                .putValue(Loader.LOADER_TYPE_KEY, StateLoader.STATE_LOADER_TYPE)
                .build();

        return (StateLoader) findShelf(storage,shelfName).buildLoader(loaderAn);
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
    public static EventLoader buildEventLoder(Storage storage, String loaderName, String shelfName)
            throws StorageException {
        Meta loaderAn = new MetaBuilder("loader")
                .putValue(Loader.LOADER_NAME_KEY, loaderName)
                .putValue(Loader.LOADER_TYPE_KEY, EventLoader.EVENT_LOADER_TYPE)
                .build();

        return (EventLoader) findShelf(storage,shelfName).buildLoader(loaderAn);
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
