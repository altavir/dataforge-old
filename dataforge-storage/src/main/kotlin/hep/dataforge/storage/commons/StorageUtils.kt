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
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.messages.Dispatcher.Companion.MESSAGE_TARGET_NODE
import hep.dataforge.io.messages.Dispatcher.Companion.TARGET_NAME_KEY
import hep.dataforge.io.messages.Dispatcher.Companion.TARGET_TYPE_KEY
import hep.dataforge.io.messages.Validator
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.storage.api.Loader
import hep.dataforge.storage.api.Loader.Companion.LOADER_TYPE_KEY
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.api.TableLoader
import hep.dataforge.storage.api.ValueIndex
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import org.slf4j.LoggerFactory
import java.util.stream.IntStream
import java.util.stream.Stream

/**
 * A helper class to builder loaders from existing storage
 *
 * @author darksnake
 */
object StorageUtils {

    val SHELF_PATH_KEY = "path"

    //    public static String loaderName(Meta loaderAnnotation) {
    //        return loaderAnnotation.getString(Loader.LOADER_NAME_KEY);
    //    }

    fun loaderType(loaderAnnotation: Meta): String {
        return loaderAnnotation.getString(LOADER_TYPE_KEY, TableLoader.TABLE_LOADER_TYPE)
    }

    fun shelfName(shelfAnnotation: Meta): String {
        return shelfAnnotation.getString(SHELF_PATH_KEY)
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

    /**
     * Stream of all loaders in the storage with corresponding relative names
     *
     * @param storage
     * @return
     */
    @JvmOverloads
    fun loaderStream(storage: Storage, recursive: Boolean = true): Stream<Loader> {
        try {
            return if (recursive) {
                Stream.concat(
                        storage.shelves().stream().flatMap { loaderStream(it) }, storage.loaders().stream()
                )
            } else {
                storage.loaders().stream()
            }
        } catch (ex: StorageException) {
            throw RuntimeException(ex)
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
    </T> */
    @Throws(StorageException::class)
    fun <T> sparsePull(index: ValueIndex<T>, from: Value, to: Value, limit: Int): Stream<T> {
        if (limit > 0) {
            if (isNumeric(from) && isNumeric(to)) {
                val a = from.double
                val b = to.double
                return sparsePull(index, a, b, limit)
            } else {
                return index.pull(from, to).limit(limit.toLong())
            }
        } else {
            return index.pull(from, to)
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
    @Throws(StorageException::class)
    fun <T> sparsePull(index: ValueIndex<T>, from: Double = index.firstKey.double, to: Double = index.lastKey.double, limit: Int): Stream<T> {
        val step = (to - from) / limit
        return IntStream.range(0, limit).mapToObj<T> { i ->
            val x = from + step * i
            try {
                index.pullOne(x + step / 2).get()
            } catch (e: StorageException) {
                throw RuntimeException(e)
            }
        }.filter { it -> it != null }.distinct()

    }


    private fun isNumeric(`val`: Value): Boolean {
        return `val`.type == ValueType.NUMBER || `val`.type == ValueType.TIME
    }

    /**
     * A simple validator that checks only name and type if present
     *
     * @param type
     * @param name
     * @return
     */
    fun defaultMessageValidator(type: String, name: String): Validator {
        return object : Validator {
            override fun isValid(message: Envelope): Boolean {
                return validate(message).getBoolean(Validator.IS_VALID_KEY)
            }

            override fun validate(message: Envelope): Meta {
                if (message.meta.hasMeta(MESSAGE_TARGET_NODE)) {
                    val target = message.meta.getMeta(MESSAGE_TARGET_NODE)
                    val targetName = target.getString(TARGET_NAME_KEY)
                    return if (targetName == name) {
                        if (!target.hasValue(TARGET_TYPE_KEY) || target.getString(TARGET_TYPE_KEY) == type) {
                            Validator.valid()
                        } else {
                            Validator.invalid("Wrong message target type")

                        }
                    } else {
                        Validator.invalid("Wrong message target name")
                    }
                } else {
                    LoggerFactory.getLogger(javaClass).debug("Envelope does not have target. Accepting by default.")
                    return Validator.valid()
                }
            }
        }

    }


    /**
     * Return shelf with given name if it does exist, otherwise builder shelf with given meta
     *
     * @param shelfName
     * @param shelfConfiguration
     * @return
     */
    fun getOrBuildShelf(storage: Storage, shelfName: String, shelfConfiguration: Meta): Storage {
        return storage.optShelf(shelfName).orElseGet { storage.buildShelf(shelfName, shelfConfiguration) }
    }

    /**
     * Create intermediate path for building loaders and shelves
     *
     * @param path
     * @return
     */
    fun buildPath(storage: Storage, path: Name): Storage {
        return if (path.length == 0) {
            storage
        } else {
            buildPath(getOrBuildShelf(storage, path.first.toString(), Meta.empty()), path.cutFirst())
        }
    }


    //TODO make stream producing renamed loaders
}
