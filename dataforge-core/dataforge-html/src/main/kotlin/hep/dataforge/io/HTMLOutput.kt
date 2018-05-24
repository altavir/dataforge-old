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

package hep.dataforge.io

import ch.qos.logback.classic.spi.ILoggingEvent
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.description.ValueDescriptor
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeType
import hep.dataforge.io.envelopes.TaglessEnvelopeType
import hep.dataforge.io.history.Record
import hep.dataforge.io.output.*
import hep.dataforge.kodex.asMap
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.tables.Table
import hep.dataforge.values.ValueType
import javafx.scene.paint.Color
import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import kotlinx.html.body
import kotlinx.html.dom.create
import kotlinx.html.dom.document
import org.w3c.dom.Document

abstract class HTMLOutput : Output {

    private val document = document { }

    private val body = document.create.body { }

    private fun renderMeta(parent: TagConsumer, obj: Meta, cfg: Meta) {
        parent.
    }

    abstract fun update(document: Document)

    override fun render(obj: Any, meta: Meta) {
        when (obj) {
            is SelfRendered -> {
                obj.render(this, meta)
            }
            is Meta -> renderMeta(obj, meta)
            is Table -> {
                //TODO add support for tab-stops
                renderText(obj.format.names.joinToString(separator = "\t"), TextColor(Color.BLUE))
                obj.rows.forEach { values ->
                    printer.println(obj.format.names.map { values[it] }.joinToString(separator = "\t"))
                }
            }
            is Envelope -> {
                val envelopeType = EnvelopeType.resolve(meta.getString("envelope.encoding", TaglessEnvelopeType.TAGLESS_ENVELOPE_TYPE))
                        ?: throw RuntimeException("Unknown envelope encoding")
                val envelopeProperties = meta.getMeta("envelope.properties", Meta.empty()).asMap { it.string }
                envelopeType.getWriter(envelopeProperties).write(stream, obj)
            }
            is ILoggingEvent -> {
                printer.println(String(logEncoder.encode(obj)))
            }
            is CharSequence -> printer.println(obj)
            is Record -> printer.println(obj)
            is ValueDescriptor -> {
                if (obj.isRequired) renderText("(*) ", Color.CYAN)
                renderText(obj.name, Color.RED)
                if (obj.isMultiple) renderText(" (mult)", Color.CYAN)
                renderText(" (${obj.type().first()})")
                if (obj.hasDefault()) {
                    val def = obj.defaultValue()
                    if (def.type == ValueType.STRING) {
                        renderText(" = \"")
                        renderText(def.string, Color.YELLOW)
                        renderText("\": ")
                    } else {
                        renderText(" = ")
                        renderText(def.string, Color.YELLOW)
                        renderText(": ")
                    }
                } else {
                    renderText(": ")
                }
                renderText(obj.info)
            }
            is NodeDescriptor -> {
                obj.childrenDescriptors().forEach { key, value ->
                    val newMeta = meta.builder
                            .setValue("text.offset", meta.getInt("text.offset", 0) + 1)
                            .setValue("text.bullet", "+")
                    renderText(key + "\n", Color.BLUE)
                    if (value.isRequired) renderText("(*) ", Color.CYAN)

                    renderText(value.name, Color.MAGENTA)

                    if (value.isMultiple) renderText(" (mult)", Color.CYAN)

                    if (!value.info.isEmpty()) {
                        renderText(": ${value.info}")
                    }
                    render(value, newMeta)
                }

                obj.valueDescriptors().forEach { key, value ->
                    val newMeta = meta.builder
                            .setValue("text.offset", meta.getInt("text.offset", 0) + 1)
                            .setValue("text.bullet", "-")
                    renderText(key + "\n", Color.BLUE)
                    render(value, newMeta)
                }
            }
            is Metoid -> { // render custom metoid
                val renderType = obj.meta.getString("@output.type", "@default")
                context.findService(OutputRenderer::class.java) { it.type == renderType }
                        ?.render(this@StreamOutput, obj, meta)
                        ?: renderMeta(obj.meta, meta)
            }
        }
        update(document)
    }
}