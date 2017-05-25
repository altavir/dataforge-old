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
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static hep.dataforge.io.messages.Dispatcher.*;
import static hep.dataforge.storage.api.Loader.LOADER_TYPE_KEY;

/**
 * A helper class to build loaders from existing storage
 *
 * @author darksnake
 */
public class StorageUtils {

    public static final String SHELF_PATH_KEY = "path";

//    public static String loaderName(Meta loaderAnnotation) {
//        return loaderAnnotation.getString(Loader.LOADER_NAME_KEY);
//    }

    public static String loaderType(Meta loaderAnnotation) {
        return loaderAnnotation.getString(LOADER_TYPE_KEY, PointLoader.POINT_LOADER_TYPE);
    }

    public static String shelfName(Meta shelfAnnotation) {
        return shelfAnnotation.getString(SHELF_PATH_KEY);
    }

//    public static void setupLoaders(Storage storage, Meta loaderConfig) throws StorageException {
//        if (loaderConfig.hasMeta("shelf")) {
//            for (Meta an : loaderConfig.getMetaList("shelf")) {
//                String shelfName = shelfName(an);
//                Storage shelf = storage.optShelf(shelfName).orElseGet(() -> storage.buildShelf(shelfName(an), an));
//                setupLoaders(shelf, an);
//            }
//        }
//
//        if (loaderConfig.hasMeta("loader")) {
//            List<? extends Meta> loaderAns = loaderConfig.getMetaList("loader");
//            for (Meta la : loaderAns) {
//                String loaderName = loaderName(la);
//                Loader current = storage.optLoader(loaderName).orElseGet(() -> storage.buildLoader(loaderName, la));
//                //If the same annotation is used - do nothing
//                if (!current.meta().equals(la)) {
//                    storage.buildLoader(loaderName, loaderConfig);
//                }
//            }
//        }
//    }

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
                    storage.shelves().stream().flatMap(entry ->
                            loaderStream(entry)
                                    .map(pair -> new Pair<>(Name.joinString(entry.getName(), pair.getKey()), pair.getValue()))
                    ),
                    storage.loaders().stream().map(entry -> new Pair<>(entry.getName(), entry))
            );
        } catch (StorageException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Use sparsePull for numeric values and simple limit for other types. Non-positive limits treated as non-existent.
     *
     * @param index
     * @param from
     * @param to
     * @param limit
     * @param <T>
     * @return
     * @throws StorageException
     */
    public static <T> Stream<T> sparsePull(ValueIndex<T> index, Value from, Value to, int limit) throws StorageException {
        if (limit > 0) {
            if (isNumeric(from) && isNumeric(to)) {
                double a = from.doubleValue();
                double b = to.doubleValue();
                return sparsePull(index, a, b, limit);
            } else {
                return index.pull(from, to).limit(limit);
            }
        } else {
            return index.pull(from, to);
        }
    }

    /**
     * Pull a uniformly distributed list of objects. It splits a region in uniform segments and returns any value from each segment.
     * If segment does not contain a value, it is skipped.
     *
     * @param index
     * @param from
     * @param to
     * @param limit
     * @param <T>
     * @return
     * @throws StorageException
     */
    public static <T> Stream<T> sparsePull(ValueIndex<T> index, double from, double to, int limit) throws StorageException {
        if (!Double.isFinite(from)) {
            from = index.getFirstKey().doubleValue();
        }
        if (!Double.isFinite(to)) {
            to = index.getLastKey().doubleValue();
        }

        double start = from;
        double step = (to - from) / limit;

        return IntStream.range(0, limit).mapToObj(i -> {
            double x = start + step * i;
            try {
                return index.pullOne(x + step / 2).get();
            } catch (StorageException e) {
                throw new RuntimeException(e);
            }
        }).filter(it -> it != null).distinct();

    }


    private static boolean isNumeric(Value val) {
        return val.valueType() == ValueType.NUMBER || val.valueType() == ValueType.TIME;
    }

    /**
     * A simple validator that checks only name and type if present
     *
     * @param type
     * @param name
     * @return
     */
    public static MessageValidator defaultMessageValidator(String type, String name) {
        return new MessageValidator() {
            @Override
            public Meta validate(Envelope message) {
                if (message.meta().hasMeta(MESSAGE_TARGET_NODE)) {
                    Meta target = message.meta().getMeta(MESSAGE_TARGET_NODE);
                    String targetName = target.getString(TARGET_NAME_KEY);
                    if (targetName.equals(name)) {
                        if (!target.hasValue(TARGET_TYPE_KEY) || target.getString(TARGET_TYPE_KEY).equals(type)) {
                            return MessageValidator.valid();
                        } else {
                            return MessageValidator.invalid("Wrong message target type");

                        }
                    } else {
                        return MessageValidator.invalid("Wrong message target name");
                    }
                } else {
                    LoggerFactory.getLogger(getClass()).debug("Envelope does not have target. Acepting by default.");
                    return MessageValidator.valid();
                }
            }
        };

    }


    /**
     * Return shelf with given name if it does exist, otherwise build shelf with given meta
     *
     * @param shelfName
     * @param shelfConfiguration
     * @return
     */
    public static Storage getOrBuildShelf(Storage storage, String shelfName, Meta shelfConfiguration) {
        return storage.optShelf(shelfName).orElseGet(() -> storage.buildShelf(shelfName, shelfConfiguration));
    }

    /**
     * Create intermediate path for building loaders and shelves
     *
     * @param path
     * @return
     */
    public static Storage buildPath(Storage storage, Name path) {
        if (path.length() == 0) {
            return storage;
        } else {
            return buildPath(getOrBuildShelf(storage, path.getFirst().toString(), Meta.empty()), path.cutFirst());
        }
    }


    //TODO make stream producing renamed loaders
}
