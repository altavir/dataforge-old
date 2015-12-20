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
import hep.dataforge.storage.api.Loader;
import static hep.dataforge.storage.api.Loader.LOADER_TYPE_KEY;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import java.util.List;

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

//    /**
//     * Сохранить существующий набор конфигураций загрузчиков в XML файл
//     *
//     * @param fileName
//     * @param ans
//     * @throws java.io.IOException
//     */
//    public static void saveLoaderMap(String fileName, Collection<Meta> ans) throws IOException {
//        MetaBuilder builder = new MetaBuilder("loaders");
//        ans.stream().filter((an) -> (an.getName().equals("loader") && an.hasValue("loadername"))).forEach((an) -> {
//            builder.putNode(an);
//        });
//        String res = new JSONMetaWriter().writeString(builder, null);
//        FileWriter writer = new FileWriter(fileName);
//        writer.append(res);
//    }
}
