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

import hep.dataforge.kodex.optional
import hep.dataforge.meta.Meta
import hep.dataforge.providers.ProvidesNames
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import java.util.*
import java.util.stream.Stream

/**
 * State loader is
 *
 * @author darksnake
 */
interface StateLoader : Loader, ValueProvider {

    override val type: String
        get() = STATE_LOADER_TYPE
    /**
     * List of all available state names (including default values if they are
     * available)
     *
     * @return
     */
    @get:ProvidesNames(ValueProvider.VALUE_TARGET)
    val valueStream: Stream<Pair<String, Value>>
    val metaStream: Stream<Pair<String, Meta>>

    /**
     * Change the state and generate corresponding StateChangedEvent
     *
     * @param path
     * @param value
     */
    fun push(path: String, value: Value)

    fun push(path: String, meta: Meta)

    fun push(path: String, value: Any) {
        if (value is Meta) {
            push(path, value)
        } else {
            push(path, Value.of(value))
        }
    }

    fun pull(path: String): Value?

    fun pullMeta(path: String): Meta?

    override fun optValue(path: String): Optional<Value> {
        return pull(path).optional
    }

    companion object {
        const val STATE_LOADER_TYPE = "state"
    }

}
