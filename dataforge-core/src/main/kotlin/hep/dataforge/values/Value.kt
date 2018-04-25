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

import hep.dataforge.utils.NamingUtils
import java.io.Serializable
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.*
import java.util.stream.Stream
import kotlin.streams.toList

/**
 * The list of supported Value types.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
enum class ValueType {
    BINARY, NUMBER, BOOLEAN, STRING, TIME, NULL
}

/**
 * An immutable wrapper class that can hold Numbers, Strings and Instant
 * objects. The general contract for Value is that it is immutable, more
 * specifically, it can't be changed after first call (it could be lazy
 * calculated though)
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
interface Value : Serializable {

    /**
     * The number representation of this value
     *
     * @return a [Number] object.
     */
    val number: Number

    /**
     * Boolean representation of this Value
     *
     * @return a boolean.
     */
    val boolean: Boolean

    val double: Double
        get() = number.toDouble()

    val int: Int
        get() = number.toInt()

    val long: Long
        get() = number.toLong()

    /**
     * Instant representation of this Value
     *
     * @return
     */
    val time: Instant

    val binary: ByteBuffer
        get() = ByteBuffer.wrap(string.toByteArray())

    /**
     * The String representation of this value
     *
     * @return a [String] object.
     */
    val string: String

    val type: ValueType

    /**
     * Return list of values representation of current value. If value is
     * instance of ListValue, than the actual list is returned, otherwise
     * immutable singleton list is returned.
     *
     * @return
     */
    val list: List<Value>
        get() = listOf(this)

    val isNull: Boolean
        get() = this.type == ValueType.NULL

    /**
     * True if it is a list value
     *
     * @return
     */
    val isList: Boolean
        get() = false

    /**
     * Return underlining object. Used for dynamic calls mostly
     */
    val value: Any

    companion object {
        const val NULL_STRING = "@null"

        val NULL: Value = NullValue()

        /**
         * Create Value from String using closest match conversion
         *
         * @param str a [String] object.
         * @return a [Value] object.
         */
        fun of(str: String): Value {

            //Trying to get integer
            if (str.isEmpty()) {
                return Value.NULL
            }

            //string constants
            if (str.startsWith("\"") && str.endsWith("\"")) {
                return StringValue(str.substring(1, str.length - 2))
            }

            try {
                val `val` = Integer.parseInt(str)
                return of(`val`)
            } catch (ignored: NumberFormatException) {
            }

            //Trying to get double
            try {
                val `val` = java.lang.Double.parseDouble(str)
                return of(`val`)
            } catch (ignored: NumberFormatException) {
            }

            //Trying to get Instant
            try {
                val `val` = Instant.parse(str)
                return of(`val`)
            } catch (ignored: DateTimeParseException) {
            }

            //Trying to parse LocalDateTime
            try {
                val `val` = LocalDateTime.parse(str).toInstant(ZoneOffset.UTC)
                return of(`val`)
            } catch (ignored: DateTimeParseException) {
            }

            if ("true" == str || "false" == str) {
                return BooleanValue.ofBoolean(str)
            }

            if (str.startsWith("[") && str.endsWith("]")) {
                //FIXME there will be a problem with nested lists because of splitting
                val strings = NamingUtils.parseArray(str)
                return Value.of(strings)
            }

            //Give up and return a StringValue
            return StringValue(str)
        }

        fun of(strings: Array<String>): Value {
            val values = ArrayList<Value>()
            for (str in strings) {
                values.add(Value.of(str))
            }
            return Value.of(values)
        }

        /**
         * create a boolean Value
         *
         * @param b a boolean.
         * @return a [Value] object.
         */
        fun of(b: Boolean): Value {
            return BooleanValue.ofBoolean(b)
        }

        fun of(d: Double): Value {
            return NumberValue(d)
        }

        fun of(i: Int): Value {
            return NumberValue(i)
        }

        fun of(l: Long): Value {
            return NumberValue(l)
        }

        fun of(bd: BigDecimal): Value {
            return NumberValue(bd)
        }

        fun of(t: LocalDateTime): Value {
            return TimeValue(t)
        }

        fun of(t: Instant): Value {
            return TimeValue(t)
        }

        fun of(vararg list: Any): Value {
            return of(Arrays.asList(*list))
        }

        fun of(list: Collection<Any>): Value {
            return when {
                list.isEmpty() -> NULL
                list.size == 1 -> of(list.first())
                else -> ListValue(list.map { Value.of(it) })
            }
        }

        fun of(vararg list: Value): Value {
            return when (list.size) {
                0 -> NULL
                1 -> list[0]
                else -> ListValue(list.toList())
            }
        }

        /**
         * Create Value from any object using closest match conversion. Throws a
         * RuntimeException if given object could not be converted to Value
         *
         * @param obj a [Object] object.
         * @return a [Value] object.
         */
        fun of(obj: Any?): Value {
            return if (obj == null) {
                Value.NULL
            } else obj as? Value ?: when {
                obj is Number -> NumberValue(obj)
                obj is Instant -> of(obj)
                obj is LocalDateTime -> TimeValue(obj)
                obj is Boolean -> BooleanValue.ofBoolean(obj)
                obj is String -> StringValue(obj)
                obj is Collection<*> -> of(obj as Collection<*>?)
                obj is Stream<*> -> of(obj.toList())
                obj.javaClass.isArray -> ListValue((obj as Array<*>).map { Value.of(it) })
                obj is Enum<*> -> of(obj.name)
                else -> of(obj.toString())
            }
        }
    }

}
