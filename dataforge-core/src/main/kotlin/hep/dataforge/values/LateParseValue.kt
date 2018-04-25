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

package hep.dataforge.values

import java.time.Instant

/**
 * A value that delays its parsing allowing to parse meta from text much faster, since the parsing is the most expensive operation
 */
class LateParseValue(str: String) : AbstractValue() {

    private val value: Value by lazy { Value.of(str) }

    override fun getNumber(): Number {
        return value.number
    }

    override fun getBoolean(): Boolean {
        return value.boolean
    }

    override fun getTime(): Instant {
        return value.time
    }

    override fun getString(): String {
        return value.string
    }

    override fun getType(): ValueType {
        return value.type
    }

    override val value: Any
        get() {
            return value.value
        }
}