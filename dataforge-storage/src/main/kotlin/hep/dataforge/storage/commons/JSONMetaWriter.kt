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
import hep.dataforge.io.MetaStreamWriter
import hep.dataforge.meta.Meta
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import javax.json.Json
import javax.json.JsonObject

/**
 * A converter from Meta object to JSON character stream
 *
 * @author Alexander Nozik
 */
class JSONMetaWriter(private val prettify: Boolean = true) : MetaStreamWriter {

    private var charset = IOUtils.UTF8_CHARSET

    override fun withCharset(charset: Charset): JSONMetaWriter {
        val res = JSONMetaWriter()
        res.charset = charset
        return res
    }

    override fun write(stream: OutputStream, meta: Meta) {
        val properties = HashMap<String, Any>()
        if (prettify) {
            properties["javax.json.stream.JsonGenerator.prettyPrinting"] = true
        }
        val writerFactory = Json.createWriterFactory(properties)
        writerFactory.createWriter(stream, charset)
                .write(metaToJson(meta))
    }

    companion object {

        var instance = JSONMetaWriter()

        fun metaToJson(meta: Meta): JsonObject {
            val res = Json.createObjectBuilder()
            // записываем все значения
            meta.getValueNames(true).forEach { key ->
                val item = meta.getValue(key).listValue()
                if (item.size == 1) {
                    res.add(key, item[0].stringValue())
                } else {
                    val array = Json.createArrayBuilder()
                    for (`val` in item) {
                        array.add(`val`.stringValue())
                    }
                    res.add(key, array)
                }
            }
            // write all meta nodes recursively
            meta.getNodeNames(true).forEach { key ->
                val item = meta.getMetaList(key)
                if (item.size == 1) {
                    res.add(key, metaToJson(item[0]))
                } else {
                    val array = Json.createArrayBuilder()
                    for (anval in item) {
                        array.add(metaToJson(anval))
                    }
                    res.add(key, array)
                }
            }
            return res.build()
        }
    }
}
