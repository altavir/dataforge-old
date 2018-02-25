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

package hep.dataforge.io.envelopes

import hep.dataforge.io.IOUtils
import hep.dataforge.io.MetaStreamReader
import hep.dataforge.io.MetaStreamWriter
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaUtils
import java.io.*
import java.nio.charset.Charset
import java.text.ParseException
import java.util.*
import java.util.function.Predicate

/**
 * Binary meta type
 * Created by darksnake on 02-Mar-17.
 */
object BinaryMetaType : MetaType {

    val BINARY_META_CODES = arrayOf<Short>(0x4249, 10)

    override val codes: List<Short> = Arrays.asList(*BINARY_META_CODES)

    override val name: String = "binary"

    override fun fileNameFilter(): Predicate<String> {
        return Predicate { str -> str.toLowerCase().endsWith(".meta") }
    }

    override val reader: MetaStreamReader = object : MetaStreamReader {

        @Throws(IOException::class, ParseException::class)
        override fun read(stream: InputStream, length: Long): MetaBuilder {
            val actualStream = if (length > 0) {
                val bytes = ByteArray(length.toInt())
                stream.read(bytes)
                ByteArrayInputStream(bytes)
            } else {
                stream
            }
            val ois = ObjectInputStream(actualStream)
            return MetaUtils.readMeta(ois)
        }

        override fun withCharset(charset: Charset): MetaStreamReader {
            //charet is ignored
            return this
        }

        override fun getCharset(): Charset {
            return IOUtils.ASCII_CHARSET
        }
    }

    override val writer = object : MetaStreamWriter {

        override fun withCharset(charset: Charset): MetaStreamWriter {
            //charset is ignored
            return this
        }

        @Throws(IOException::class)
        override fun write(stream: OutputStream, meta: Meta) {
            MetaUtils.writeMeta(ObjectOutputStream(stream), meta)
            stream.write('\r'.toInt())
            stream.write('\n'.toInt())
        }
    }

}
