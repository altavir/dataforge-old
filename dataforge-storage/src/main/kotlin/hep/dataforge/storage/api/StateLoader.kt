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
import hep.dataforge.providers.ProvidesNames
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider

/**
 * State loader is
 *
 * @author darksnake
 */
interface StateLoader : Loader, ValueProvider {

    /**
     * List of all available state names (including default values if they are
     * available)
     *
     * @return
     */
    @get:ProvidesNames(ValueProvider.VALUE_TARGET)
    val stateNames: Set<String>

    /**
     * Change the state and generate corresponding StateChangedEvent
     *
     * @param path
     * @param value
     * @throws hep.dataforge.exceptions.StorageException
     */
    @Throws(StorageException::class)
    fun pushState(path: String, value: Value)

    @Throws(StorageException::class)
    fun pushState(path: String, value: Any) {
        pushState(path, Value.of(value))
    }

    companion object {

        val STATE_LOADER_TYPE = "state"
    }

}
