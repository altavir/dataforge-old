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
package hep.dataforge.plots

import hep.dataforge.description.ValueDef
import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.io.envelopes.SimpleEnvelope
import hep.dataforge.io.envelopes.Wrapper.Companion.WRAPPER_TYPE_KEY
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Meta
import java.io.ObjectStreamException
import java.io.OutputStream
import java.io.Serializable
import java.util.*

/**
 * Набор графиков (plot) в одном окошке (frame) с общими осями.
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "title", info = "The title of the plot. By default the name of the Content is taken.")
interface PlotFrame : Configurable, Serializable {

    /**
     * Get root plot node
     *
     * @return
     */
    val plots: PlotGroup

    /**
     * Add or replace registered plottable
     *
     * @param plotable
     */
    fun add(plotable: Plottable) {
        plots.add(plotable)
    }

    /**
     * Add (replace) all plottables to the frame
     *
     * @param plottables
     */
    fun addAll(plottables: Iterable<Plottable>) {
        for (pl in plottables) {
            add(pl)
        }
    }

    /**
     * Update all plottables. Remove the ones not present in a new set
     *
     * @param plottables
     */
    fun setAll(plottables: Collection<Plottable>) {
        clear()
        plottables.forEach{ this.add(it) }
    }

    /**
     * Remove plottable with given name
     *
     * @param plotName
     */
    fun remove(plotName: String) {
        plots.remove(plotName)
    }

    /**
     * Remove all plottables
     */
    fun clear() {
        plots.clear()
    }


    /**
     * Opt the plottable with the given name
     *
     * @param name
     * @return
     */
    fun opt(name: String): Optional<Plot>

    operator fun get(name: String): Plot {
        return opt(name).orElseThrow { NameNotFoundException(name) }
    }

    /**
     * Save plot as image
     *
     * @param stream
     * @param config
     */
    open fun asImage(stream: OutputStream, config: Meta) {
        throw UnsupportedOperationException()
    }

    //    default Object writeReplace() throws ObjectStreamException {
    //        return new PlotFrameEnvelope(wrapper.wrap(this));
    //    }

    /**
     * Use exclusively for plot frame serialization
     */
    class PlotFrameEnvelope : SimpleEnvelope {

        constructor() {}

        constructor(envelope: Envelope) : super(envelope.meta, envelope.data) {}

        @Throws(ObjectStreamException::class)
        private fun readResolve(): Any {
            return wrapper.unWrap(this)
        }
    }


    class Wrapper : hep.dataforge.io.envelopes.Wrapper<PlotFrame> {

        override val name: String
            get() = PLOT_FRAME_WRAPPER_TYPE

        override val type: Class<PlotFrame>
            get() = PlotFrame::class.java

        override fun wrap(obj: PlotFrame): Envelope {
            val rootEnv = PlotGroup.WRAPPER.wrap(obj.plots)

            val builder = EnvelopeBuilder()
                    .setMeta(rootEnv.meta)
                    .setData(rootEnv.data)
                    .setContentType("wrapper")
                    .setMetaValue(WRAPPER_TYPE_KEY, PLOT_FRAME_WRAPPER_TYPE)
                    .setMetaValue(PLOT_FRAME_CLASS_KEY, obj.javaClass.name)
                    .putMetaNode(PLOT_FRAME_META_KEY, obj.config)
            return builder.build()
        }

        override fun unWrap(envelope: Envelope): PlotFrame {
            val root = PlotGroup.WRAPPER.unWrap(envelope)

            val plotFrameClassName = envelope.meta.getString(PLOT_FRAME_CLASS_KEY, "hep.dataforge.plots.JFreeChartFrame")
            val plotMeta = envelope.meta.getMetaOrEmpty(PLOT_FRAME_META_KEY)

            try {
                val frame = Class.forName(plotFrameClassName).getConstructor().newInstance() as PlotFrame
                frame.configure(plotMeta)
                frame.addAll(root)
                frame.plots.configure(root.config)

                return frame
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }

        }

        companion object {
            val PLOT_FRAME_WRAPPER_TYPE = "df.plots.frame"
            val PLOT_FRAME_CLASS_KEY = "frame.class"
            val PLOT_FRAME_META_KEY = "frame.meta"
        }

    }

    companion object {

        val wrapper = Wrapper()
    }

}
