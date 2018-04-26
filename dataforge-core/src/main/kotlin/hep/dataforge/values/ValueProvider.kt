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

import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.providers.Path
import hep.dataforge.providers.Provider
import hep.dataforge.providers.Provides
import java.time.Instant
import java.util.*
import java.util.function.Supplier

interface ValueProvider {

    @JvmDefault
    fun hasValue(path: String): Boolean {
        return optValue(path).isPresent
    }

    @Provides(VALUE_TARGET)
    fun optValue(path: String): Optional<Value>

    @JvmDefault
    fun getValue(path: String): Value {
        return optValue(path).orElseThrow<NameNotFoundException>({ NameNotFoundException(path) })
    }

    @Provides(BOOLEAN_TARGET)
    @JvmDefault
    fun optBoolean(name: String): Optional<Boolean> {
        return optValue(name).map<Boolean> { it.boolean }
    }

    @JvmDefault
    fun getBoolean(name: String, def: Boolean): Boolean {
        return optValue(name).map<Boolean> { it.boolean }.orElse(def)
    }

    @JvmDefault
    fun getBoolean(name: String, def: Supplier<Boolean>): Boolean {
        return optValue(name).map<Boolean> { it.boolean }.orElseGet(def)
    }

    @JvmDefault
    fun getBoolean(name: String): Boolean {
        return getValue(name).boolean
    }

    @Provides(NUMBER_TARGET)
    @JvmDefault
    fun optNumber(name: String): Optional<Number> {
        return optValue(name).map<Number> { it.number }
    }

    @JvmDefault
    fun getDouble(name: String, def: Double): Double {
        return optValue(name).map<Double> { it.double }.orElse(def)
    }

    @JvmDefault
    fun getDouble(name: String, def: Supplier<Double>): Double {
        return optValue(name).map<Double> { it.double }.orElseGet(def)
    }

    @JvmDefault
    fun getDouble(name: String): Double {
        return getValue(name).double
    }

    @JvmDefault
    fun getInt(name: String, def: Int): Int {
        return optValue(name).map<Int> { it.int }.orElse(def)
    }

    @JvmDefault
    fun getInt(name: String, def: Supplier<Int>): Int {
        return optValue(name).map<Int> { it.int }.orElseGet(def)

    }

    @JvmDefault
    fun getInt(name: String): Int {
        return getValue(name).int
    }

    @JvmDefault
    @Provides(STRING_TARGET)
    fun optString(name: String): Optional<String> {
        return optValue(name).map<String> { it.string }
    }

    @JvmDefault
    fun getString(name: String, def: String): String {
        return optString(name).orElse(def)
    }

    @JvmDefault
    fun getString(name: String, def: Supplier<String>): String {
        return optString(name).orElseGet(def)
    }

    @JvmDefault
    fun getString(name: String): String {
        return getValue(name).string
    }

    @JvmDefault
    fun getValue(name: String, def: Any): Value {
        return optValue(name).orElse(Value.of(def))
    }

    @JvmDefault
    fun getValue(name: String, def: Supplier<Value>): Value {
        return optValue(name).orElseGet(def)
    }

    @Provides(TIME_TARGET)
    @JvmDefault
    fun optTime(name: String): Optional<Instant> {
        return optValue(name).map { it.time }
    }

    @JvmDefault
    fun getStringArray(name: String): Array<String> {
        val vals = getValue(name).list
        return Array(vals.size) { vals[it].string }
    }

    @JvmDefault
    fun getStringArray(name: String, def: Supplier<Array<String>>): Array<String> {
        return if (this.hasValue(name)) {
            getStringArray(name)
        } else {
            def.get()
        }
    }

    @JvmDefault
    fun getStringArray(name: String, def: Array<String>): Array<String> {
        return if (this.hasValue(name)) {
            getStringArray(name)
        } else {
            def
        }
    }

    companion object {

        const val VALUE_TARGET = "value"
        const val STRING_TARGET = "string"
        const val NUMBER_TARGET = "number"
        const val BOOLEAN_TARGET = "boolean"
        const val TIME_TARGET = "time"

        /**
         * Build a meta provider from given general provider
         *
         * @param provider
         * @return
         */
        fun buildFrom(provider: Provider): ValueProvider {
            return provider as? ValueProvider ?: object : ValueProvider {
                override fun optValue(path: String): Optional<Value> {
                    return provider.provide(Path.of(path, VALUE_TARGET)).map<Value> { Value::class.java.cast(it) }
                }
            }
        }
    }
}
