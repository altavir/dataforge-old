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
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.*


/**
 * Created by darksnake on 06-Aug-16.
 */
object ValueUtils {

    val NUMBER_COMPARATOR: Comparator<Number> = NumberComparator()
    val VALUE_COMPARATOR: Comparator<Value> = ValueComparator()

    fun compare(val1: Value, val2: Value): Int {
        return when (val1.type) {
            ValueType.NUMBER -> NUMBER_COMPARATOR.compare(val1.number, val2.number)
            ValueType.BOOLEAN -> java.lang.Boolean.compare(val1.boolean, val2.boolean)
            ValueType.STRING ->
                //use alphanumeric comparator here
                val1.string.compareTo(val2.string)
            ValueType.TIME -> val1.time.compareTo(val2.time)
            ValueType.NULL -> if (val2.type == ValueType.NULL) 0 else -1
            ValueType.BINARY -> TODO()
        }
    }

    /**
     * Checks if given value is between `val1` and `val1`. Could
     * throw ValueConversionException if value conversion is not possible.
     *
     * @param val1
     * @param val2
     * @return
     */
    @JvmStatic
    fun isBetween(`val`: Value, val1: Value, val2: Value): Boolean {
        return compare(`val`, val1) > 0 && compare(`val`, val2) < 0 || compare(`val`, val2) > 0 && compare(`val`, val1) < 0

    }

    fun isBetween(`val`: Any, val1: Value, val2: Value): Boolean {
        return isBetween(Value.of(`val`), val1, val2)
    }


    /**
     * Fast and compact serialization for values
     *
     * @param oos
     * @param value
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeValue(oos: ObjectOutput, value: Value) {
        if (value.isList) {
            oos.write('L'.toInt()) // List designation
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
                    //TODO add encding specification
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
                else -> {
                    oos.write('C'.toInt())//custom
                    oos.writeObject(value)
                }
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
    fun readValue(ois: ObjectInput): Value {
        val c = ois.read()
        when (c.toChar()) {
            'L' -> {
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
                return Value.of(time)
            }
            'S' -> return Value.of(IOUtils.readString(ois))
            'D' -> return Value.of(ois.readDouble())
            'I' -> return Value.of(ois.readInt())
            'B' -> {
                val intSize = ois.readShort()
                val intBytes = ByteArray(intSize.toInt())
                ois.read(intBytes)
                val scale = ois.readInt()
                val bdc = BigDecimal(BigInteger(intBytes), scale)
                return Value.of(bdc)
            }
            'N' -> return Value.of(ois.readObject())
            '+' -> return BooleanValue.TRUE
            '-' -> return BooleanValue.FALSE
            'C' -> return ois.readObject() as Value
            else -> throw RuntimeException("Wrong value serialization format. Designation $c is unexpected")
        }
    }

    private class ValueComparator : Comparator<Value>, Serializable {
        override fun compare(o1: Value, o2: Value): Int {
            return ValueUtils.compare(o1, o2)
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
            private val RELATIVE_NUMERIC_PRECISION = 1e-5

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
