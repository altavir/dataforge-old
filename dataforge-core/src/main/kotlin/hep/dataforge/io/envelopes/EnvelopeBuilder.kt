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

import hep.dataforge.data.binary.Binary
import hep.dataforge.data.binary.BufferedBinary
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder

import java.io.ByteArrayOutputStream
import java.io.ObjectStreamException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.function.Consumer

/**
 * The convenient builder for envelopes
 *
 * @author Alexander Nozik
 */
class EnvelopeBuilder : Envelope {

    /**
     * Get modifiable meta builder for this envelope
     *
     * @return
     */
    private var metaBuilder = MetaBuilder("envelope")

    //initializing with empty buffer
    override var data: Binary = BufferedBinary(ByteArray(0))
        private set

    constructor(envelope: Envelope) {
        this.metaBuilder = envelope.meta.builder
        this.data = envelope.data
    }

    constructor() {

    }

    fun setMeta(annotation: Meta): EnvelopeBuilder {
        this.metaBuilder = annotation.builder
        return this
    }

    /**
     * Helper to fast put node to envelope meta
     *
     * @param element
     * @return
     */
    fun putMetaNode(nodeName: String, element: Meta): EnvelopeBuilder {
        this.metaBuilder.putNode(nodeName, element)
        return this
    }

    fun putMetaNode(element: Meta): EnvelopeBuilder {
        this.metaBuilder.putNode(element)
        return this
    }

    /**
     * Helper to fast put value to meta root
     *
     * @param name
     * @param value
     * @return
     */
    fun putMetaValue(name: String, value: Any): EnvelopeBuilder {
        this.metaBuilder.putValue(name, value)
        return this
    }

    fun setData(data: Binary): EnvelopeBuilder {
        this.data = data
        return this
    }

    fun setData(data: ByteBuffer): EnvelopeBuilder {
        this.data = BufferedBinary(data)
        return this
    }

    fun setData(data: ByteArray): EnvelopeBuilder {
        this.data = BufferedBinary(data)
        return this
    }

    fun setData(data: Consumer<OutputStream>): EnvelopeBuilder {
        val baos = ByteArrayOutputStream()
        data.accept(baos)
        return setData(baos.toByteArray())
    }

    fun setContentType(type: String): EnvelopeBuilder {
        putMetaValue("@envelope.type", type)
        return this
    }

    fun setContentDescription(description: String): EnvelopeBuilder {
        putMetaValue("@envelope.description", description)
        return this
    }

    override fun getMeta(): MetaBuilder {
        return metaBuilder
    }

    fun build(): Envelope {
        return SimpleEnvelope(metaBuilder, data)
    }

    @Throws(ObjectStreamException::class)
    private fun writeReplace(): Any {
        return SimpleEnvelope(this.meta, data)
    }
}
