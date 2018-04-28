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

import hep.dataforge.io.IOUtils
import hep.dataforge.providers.Path
import hep.dataforge.providers.Provider
import hep.dataforge.values.ValueUtils.asValueProvider
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*

data class ValueRange(override val start: Value, override val endInclusive: Value) : ClosedRange<Value>{
    operator fun contains(any: Any): Boolean{
        return contains(Value.of(any))
    }
}

operator fun Value.rangeTo(other: Value): ValueRange = ValueRange(this, other)


/**
 * Created by darksnake on 06-Aug-16.
 */
object ValueUtils {

    val NUMBER_COMPARATOR: Comparator<Number> = NumberComparator()

    /**
     * Fast and compact serialization for values
     *
     * @param oos
     * @param value
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun writeValue(oos: ObjectOutput, value: Value) {
        if (value.isList) {
            oos.write('*'.toInt()) // List designation
            oos.writeShort(value.list.size)
            for (subValue in value.list) {
                writeValue(oos, subValue)
            }
        } else {
            when (value.type) {
                ValueType.NULL -> oos.writeChar('0'.toInt()) // null
                ValueType.TIME -> {
                    oos.write('T'.toInt())//Instant
                    oos.writeLong(value.time.epochSecond)
                    oos.writeLong(value.time.nano.toLong())
                }
                ValueType.STRING -> {
                    oos.writeChar('S'.toInt())//String
                    IOUtils.writeString(oos, value.string)
                }
                ValueType.NUMBER -> {
                    val num = value.number
                    when (num) {
                        is Double -> {
                            oos.write('D'.toInt()) // double
                            oos.writeDouble(num.toDouble())
                        }
                        is Int -> {
                            oos.write('I'.toInt()) // integer
                            oos.writeInt(num.toInt())
                        }
                        is Long ->{
                            oos.write('L'.toInt())
                            oos.writeLong(num.toLong())
                        }
                        is BigDecimal -> {
                            oos.write('B'.toInt()) // BigDecimal
                            val bigInt = num.unscaledValue().toByteArray()
                            val scale = num.scale()
                            oos.writeShort(bigInt.size)
                            oos.write(bigInt)
                            oos.writeInt(scale)
                        }
                        else -> {
                            oos.write('N'.toInt()) //custom number
                            oos.writeObject(num)
                        }
                    }
                }
                ValueType.BOOLEAN -> if (value.boolean) {
                    oos.write('+'.toInt()) //true
                } else {
                    oos.write('-'.toInt()) // false
                }
                ValueType.BINARY -> {
                    val binary = value.binary
                    oos.write('X'.toInt())
                    oos.write(binary.limit())
                    oos.write(binary.array())
                }
//                else -> {
//                    oos.write('C'.toInt())//custom
//                    oos.writeObject(value)
//                }
            }
        }
    }

    /**
     * Value deserialization
     *
     * @param ois
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    @JvmStatic
    fun readValue(ois: ObjectInput): Value {
        val c = ois.read()
        when (c.toChar()) {
            '*' -> {
                val listSize = ois.readShort()
                val valueList = ArrayList<Value>()
                for (i in 0 until listSize) {
                    valueList.add(readValue(ois))
                }
                return Value.of(valueList)
            }
            '0' -> return Value.NULL
            'T' -> {
                val time = Instant.ofEpochSecond(ois.readLong(), ois.readLong())
                return time.asValue()
            }
            'S' -> return IOUtils.readString(ois).asValue()
            'D' -> return ois.readDouble().asValue()
            'I' -> return ois.readInt().asValue()
            'L' -> return ois.readLong().asValue()
            'B' -> {
                val intSize = ois.readShort()
                val intBytes = ByteArray(intSize.toInt())
                ois.read(intBytes)
                val scale = ois.readInt()
                val bdc = BigDecimal(BigInteger(intBytes), scale)
                return bdc.asValue()
            }
            'N' -> return Value.of(ois.readObject())
            'X' -> {
                val length = ois.read()
                val buffer = ByteArray(length)
                ois.readFully(buffer)
                return BinaryValue(ByteBuffer.wrap(buffer))
            }
            '+' -> return BooleanValue.TRUE
            '-' -> return BooleanValue.FALSE
            '?' -> return ois.readObject() as Value // Read as custom object. Currently reserved
            else -> throw RuntimeException("Wrong value serialization format. Designation $c is unexpected")
        }
    }

    /**
     * Build a meta provider from given general provider
     *
     * @param provider
     * @return
     */
    @JvmStatic
    fun Provider.asValueProvider(): ValueProvider {
        return this as? ValueProvider ?: object : ValueProvider {
            override fun optValue(path: String): Optional<Value> {
                return this@asValueProvider.provide(Path.of(path, ValueProvider.VALUE_TARGET)).map<Value> { Value::class.java.cast(it) }
            }
        }
    }

    private class NumberComparator : Comparator<Number>, Serializable {

        override fun compare(x: Number, y: Number): Int {
            val d1 = x.toDouble()
            val d2 = y.toDouble()
            return if ((d1 != 0.0 || d2 != 0.0) && Math.abs(d1 - d2) / Math.max(d1, d2) < RELATIVE_NUMERIC_PRECISION) {
                0
            } else if (isSpecial(x) || isSpecial(y)) {
                java.lang.Double.compare(d1, d2)
            } else {
                toBigDecimal(x).compareTo(toBigDecimal(y))
            }
        }

        companion object {
            private const val RELATIVE_NUMERIC_PRECISION = 1e-5

            private fun isSpecial(x: Number): Boolean {
                val specialDouble = x is Double && (java.lang.Double.isNaN(x) || java.lang.Double.isInfinite(x))
                val specialFloat = x is Float && (java.lang.Float.isNaN(x) || java.lang.Float.isInfinite(x))
                return specialDouble || specialFloat
            }

            private fun toBigDecimal(number: Number): BigDecimal {
                if (number is BigDecimal) {
                    return number
                }
                if (number is BigInteger) {
                    return BigDecimal(number)
                }
                if (number is Byte || number is Short
                        || number is Int || number is Long) {
                    return BigDecimal(number.toLong())
                }
                if (number is Float || number is Double) {
                    return BigDecimal(number.toDouble())
                }

                try {
                    return BigDecimal(number.toString())
                } catch (e: NumberFormatException) {
                    throw RuntimeException("The given number (\"" + number
                            + "\" of class " + number.javaClass.name
                            + ") does not have a parsable string representation", e)
                }

            }
        }
    }

}
