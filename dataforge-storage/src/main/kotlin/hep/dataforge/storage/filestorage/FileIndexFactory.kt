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
package hep.dataforge.storage.filestorage

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.storage.commons.DefaultIndex
import hep.dataforge.storage.commons.ValueProviderIndex
import hep.dataforge.values.ValueProvider
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Function

/**
 * A factory for file indexes
 *
 * @author Alexander Nozik
 */
class FileIndexFactory(private val context: Context, private val uri: String) : ContextAware {
    private val envelope: FileEnvelope by lazy{
        val file = Paths.get(uri)
        if (Files.isReadable(file)) {
            FileEnvelope.open(uri, true)
        } else {
            throw RuntimeException("Can't read file $uri")
        }
    }

    init {
        if (uri.isEmpty()) {
            throw IllegalArgumentException("Uri is empty")
        }
    }

    override fun getContext(): Context {
        return context
    }

    /**
     * Build an index which uses entry number for search
     *
     * @param <T>
     * @param transformation transformation from string to a given object type
     * @return
    </T> */
    fun <T> buildDefaultIndex(transformation: Function<String, T>): DefaultIndex<T> {
        return DefaultIndex(asIterable(transformation))
    }

    /**
     * Build index for elements which implements ValueProvider interface
     *
     * @param <T>
     * @param valueName
     * @param transformation
     * @return
    </T> */
    fun <T : ValueProvider> buildProviderStreamIndex(valueName: String, transformation: Function<String, T>): ValueProviderIndex<T> {
        return ValueProviderIndex(asIterable(transformation), valueName)
    }

    private fun <T> asIterable(transformation: Function<String, T>): Iterable<T> {
        return  object : Iterable<T>{
            override fun iterator(): Iterator<T> {
                return buildIterator(transformation)
            }

        }
    }

    private fun <T> buildIterator(transformation: Function<String, T>): Iterator<T> {
        try {
            val env = this.envelope
            return env.data.lines().map(transformation).iterator()
        } catch (ex: IOException) {
            throw RuntimeException("Cant operate file envelope $uri", ex)
        }

    }

    /**
     * Invalidate this factory and force it to reload file envelope
     */
    fun invalidate() {
        try {
            this.envelope.close()
        } catch (ex: Exception) {
            LoggerFactory.getLogger(javaClass).error("Can't close the file envelope $uri", ex)
        }
    }

}
