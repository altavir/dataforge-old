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
import hep.dataforge.names.Name;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import javafx.util.Pair;

import java.util.List;
import java.util.stream.Stream;

import static hep.dataforge.storage.api.Loader.LOADER_TYPE_KEY;

/**
 * A helper class to build loaders from existing storage
 *
 * @author darksnake
 */
public class StorageUtils {

    public static final String SHELF_PATH_KEY = "path";

    public static String loaderName(Meta loaderAnnotation) {
        return loaderAnnotation.getString(Loader.LOADER_NAME_KEY);
    }

    public static String loaderType(Meta loaderAnnotation) {
        return loaderAnnotation.getString(LOADER_TYPE_KEY, PointLoader.POINT_LOADER_TYPE);
    }

    public static String shelfName(Meta shelfAnnotation) {
        return shelfAnnotation.getString(SHELF_PATH_KEY);
    }

    public static void setupLoaders(Storage storage, Meta loaderConfig) throws StorageException {
        if (loaderConfig.hasNode("shelf")) {
            for (Meta an : loaderConfig.getNodes("shelf")) {
                String shelfName = shelfName(an);
                Storage shelf;

                if (storage.hasShelf(shelfName)) {
                    shelf = storage.getShelf(shelfName);
                } else {
                    shelf = storage.buildShelf(shelfName(an), an);
                }
                setupLoaders(shelf, an);
            }
        }

        if (loaderConfig.hasNode("loader")) {
            List<? extends Meta> loaderAns = loaderConfig.getNodes("loader");
            for (Meta la : loaderAns) {
                String loaderName = loaderName(la);
                if (!storage.hasLoader(loaderName)) {
                    storage.buildLoader(la);
                } else {
                    Loader currentLoader = storage.getLoader(loaderName);
                    //If the same annotation is used - do nothing
                    if (!currentLoader.meta().equals(la)) {
                        storage.buildLoader(loaderConfig);
                    }
                }
            }
        }
    }

    public static Meta getErrorMeta(Throwable err) {
        return new MetaBuilder("error")
                .putValue("type", err.getClass().getName())
                .putValue("message", err.getMessage())
                .build();
    }

    /**
     * Stream of all loaders in the storage with corresponding relative names
     *
     * @param storage
     * @return
     */
    public static Stream<Pair<String, Loader>> loaderStream(Storage storage) {
        try {
            return Stream.concat(
                    storage.shelves().entrySet().stream().flatMap(entry -> loaderStream(entry.getValue())
                            .map(pair -> new Pair<>(Name.joinString(entry.getKey(), pair.getKey()), pair.getValue()))),
                    storage.loaders().entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
            );
        } catch (StorageException ex) {
            throw new RuntimeException(ex);
        }
    }

    //TODO make stream producing renamed loaders
}
