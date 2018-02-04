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

package dataforge.io.envelopes

import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.io.envelopes.TaglessEnvelopeType
import hep.dataforge.meta.MetaBuilder
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

class TaglessEnvelopeTest {
    private val envelope = EnvelopeBuilder()
            .setMeta(MetaBuilder()
                    .putValue("myValue", 12)
            ).setData("Всем привет!".toByteArray(Charset.forName("UTF-8")))

    private val envelopeType = TaglessEnvelopeType.instance

    @Test
    @Throws(IOException::class)
    fun testWriteRead() {
        val baos = ByteArrayOutputStream()
        envelopeType.writer.write(baos, envelope)

        println(String(baos.toByteArray()))

        val bais = ByteArrayInputStream(baos.toByteArray())
        val restored = envelopeType.reader.read(bais)

        assertEquals(String(restored.data.buffer.array(), Charsets.UTF_8), "Всем привет!")
    }

    @Test
    @Throws(IOException::class)
    fun testShortForm() {
        val envString = "<meta myValue=\"12\"/>\n" +
                "#~DATA~#\n" +
                "Всем привет!"
        println(envString)
        val bais = ByteArrayInputStream(envString.toByteArray(charset("UTF-8")))
        val restored = envelopeType.reader.read(bais)

        assertEquals(String(restored.data.buffer.array(), Charsets.UTF_8), "Всем привет!")
    }
}