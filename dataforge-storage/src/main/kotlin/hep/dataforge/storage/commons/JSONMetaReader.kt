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
package hep.dataforge.storage.commons

import hep.dataforge.io.IOUtils
import hep.dataforge.io.MetaStreamReader
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.values.Value
import java.io.*
import java.nio.charset.Charset
import java.text.ParseException
import javax.json.*

/**
 * Reader for JSON meta
 *
 * @author Alexander Nozik
 */
class JSONMetaReader : MetaStreamReader {
    internal var charset = IOUtils.UTF8_CHARSET

    override fun withCharset(charset: Charset): MetaStreamReader {
        this.charset = charset
        return this
    }

    override fun getCharset(): Charset {
        return charset
    }

    @Throws(IOException::class, ParseException::class)
    override fun read(stream: InputStream, length: Long): MetaBuilder {
        if (length == 0L) {
            return MetaBuilder("")
        } else if (length > 0) {
            val buffer = ByteArray(length.toInt())
            stream.read(buffer)
            return fromString(String(buffer, getCharset()))
        } else {
            val baos = ByteArrayOutputStream()
            var braceCounter = 0

            var nextByte: Int
            var stopFlag = false
            while (!stopFlag && stream.available() > 0) {
                nextByte = stream.read()

                //The first symbol is required to be '{'
                if (nextByte == '{'.toInt()) {
                    braceCounter++
                } else if (nextByte == '}'.toInt()) {
                    braceCounter--
                }
                baos.write(nextByte)
                if (braceCounter == 0) {
                    stopFlag = true
                }
            }

            val ins = ByteArrayInputStream(baos.toByteArray())

            return toMeta(Json.createReader(InputStreamReader(ins, getCharset())).readObject())
        }
    }

    @Throws(ParseException::class)
    fun toMeta(source: JsonObject): MetaBuilder {
        return toMeta("", source)
    }

    @Throws(ParseException::class)
    fun fromString(string: String): MetaBuilder {
        return toMeta(Json.createReader(StringReader(string)).readObject())
    }

    @Throws(ParseException::class)
    fun toMeta(name: String, source: JsonObject): MetaBuilder {
        val builder = MetaBuilder(name)
        for ((key, value) in source) {
            putJsonValue(builder, key, value)
        }
        return builder
    }

    @Throws(ParseException::class)
    private fun putJsonValue(builder: MetaBuilder, key: String, value: JsonValue) {
        when (value.valueType) {
            JsonValue.ValueType.OBJECT -> builder.putNode(toMeta(key, value as JsonObject))
            JsonValue.ValueType.ARRAY -> {
                val array = value as JsonArray
                for (i in array.indices) {
                    putJsonValue(builder, key, array[i])
                }
            }
            JsonValue.ValueType.FALSE -> builder.putValue(key, Value.of(false))
            JsonValue.ValueType.TRUE -> builder.putValue(key, Value.of(true))
            JsonValue.ValueType.STRING -> builder.putValue(key, normalizeString(value as JsonString))
            JsonValue.ValueType.NUMBER -> builder.putValue(key, (value as JsonNumber).bigDecimalValue())
            JsonValue.ValueType.NULL -> builder.putValue(key, Value.getNull())
        }
    }

    private fun normalizeString(value: JsonString): String {
        return value.string
    }

}
