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
package hep.dataforge.storage.api

import hep.dataforge.exceptions.StorageException
import hep.dataforge.meta.Meta

import java.util.function.Function
import java.util.stream.Stream

/**
 * @author Alexander Nozik
 */
interface Index<T> {

    /**
     * Return a stream of suppliers of objects corresponding to query
     *
     * @param query
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun query(query: Meta): Stream<T>


    /**
     * Create new index that uses a transformation for each of this index result items.
     *
     * @param <R>
     * @param transformation
     * @return
    </R> */
    fun <R> transform(transformation: Function<T, R>): Index<R> {
        val theIndex = this
        return object : Index<R> {
            override fun query(query: Meta): Stream<R> {
                return theIndex.query(query).map { transformation.apply(it) }
            }

        }
    }

}
