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

import hep.dataforge.exceptions.StorageException

/**
 * The Object loader contains one o several Java object fragments with common loader annotation.
 * @author darksnake
 * @param <T>
</T> */
interface ObjectLoader<T> : Loader {

    fun fragmentNames(): Collection<String>

    @Throws(StorageException::class)
    fun pull(fragmentName: String): T

    @Throws(StorageException::class)
    fun pull(): T {
        return pull(DEFAULT_FRAGMENT_NAME)
    }

    @Throws(StorageException::class)
    fun push(fragmentName: String, data: T)

    @Throws(StorageException::class)
    fun push(data: T) {
        push(DEFAULT_FRAGMENT_NAME, data)
    }

    override val type: String
        get() = ObjectLoader.OBJECT_LOADER_TYPE


    companion object {

        val OBJECT_LOADER_TYPE = "object"
        val DEFAULT_FRAGMENT_NAME = ""
    }
}
