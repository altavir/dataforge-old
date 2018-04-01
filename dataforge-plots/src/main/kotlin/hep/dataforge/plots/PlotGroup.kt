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

import hep.dataforge.description.NodeDescriptor
import hep.dataforge.io.envelopes.DefaultEnvelopeType
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.io.envelopes.EnvelopeType
import hep.dataforge.io.envelopes.Wrapper.Companion.WRAPPER_TYPE_KEY
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaNode.DEFAULT_META_NAME
import hep.dataforge.meta.SimpleConfigurable
import hep.dataforge.names.Name
import hep.dataforge.providers.Provider
import hep.dataforge.providers.Provides
import hep.dataforge.providers.ProvidesNames
import hep.dataforge.utils.ReferenceRegistry
import javafx.util.Pair
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.stream.Stream

/**
 * A group of plottables. It could store Plots as well as other plot groups.
 */
class PlotGroup : SimpleConfigurable, Plottable, Provider, PlotListener, Iterable<Plottable> {

    //    public PlotGroup(String name, NodeDescriptor descriptor) {
    //        this.name = name;
    //        this.descriptor = descriptor;
    //    }

    override val name: Name
    private var descriptor = NodeDescriptor.empty("group")

    private val plots = HashMap<Name, Plottable>()
    private val listeners = ReferenceRegistry<PlotListener>()

    val children: Collection<Plottable>
        get() = plots.values


    constructor(name: String) {
        this.name = Name.ofSingle(name)
    }

    constructor(name: String, descriptor: NodeDescriptor) {
        this.name = Name.ofSingle(name)
        this.descriptor = descriptor
    }

    private fun getNameForListener(arg: Name): Name {
        return Name.join(name, arg)
    }

    override fun dataChanged(name: Name, plot: Plot) {
        listeners.forEach { l -> l.dataChanged(getNameForListener(name), plot) }
    }

    override fun metaChanged(name: Name, plottable: Plottable, laminate: Laminate) {
        listeners.forEach { l -> l.metaChanged(getNameForListener(name), plottable, laminate.withLayer(config).cleanup()) }
    }

    override fun plotAdded(name: Name, plottable: Plottable) {
        listeners.forEach { l -> l.plotAdded(getNameForListener(name), plottable) }
    }

    override fun plotRemoved(name: Name) {
        listeners.forEach { l -> l.plotRemoved(getNameForListener(name)) }
    }

    /**
     * Recursively notify listeners about all added plots
     *
     * @param plot
     */
    private fun notifyPlotAdded(plot: Plottable) {
        plotAdded(plot.name, plot)
        metaChanged(plot.name, plot, Laminate(plot.config))
        if (plot is PlotGroup) {
            plot.children.forEach { plot.notifyPlotAdded(it) }
        }
    }

    @Synchronized
    fun add(plot: Plottable): PlotGroup {
        this.plots[plot.name] = plot
        plot.addListener(this)

        notifyPlotAdded(plot)
        return this
    }

    private fun notifyPlotRemoved(plot: Plottable) {
        if (plot is PlotGroup) {
            plot.children.forEach { plot.notifyPlotRemoved(it) }
        }
        //remove children first
        plotRemoved(plot.name)
    }

    /**
     * Recursive remove a plot
     *
     * @param name
     * @return
     */
    @Synchronized
    fun remove(name: String): PlotGroup {
        return remove(Name.ofSingle(name))
    }

    @Synchronized
    fun remove(name: Name): PlotGroup {
        if (name.length == 1) {
            val removed = plots.remove(name)
            if (removed != null) {
                notifyPlotRemoved(removed)
                removed.removeListener(this)
            }
        } else {
            opt(name.cutLast()).ifPresent { group ->
                if (group is PlotGroup) {
                    group.remove(name.last)
                }
            }
        }
        return this
    }

    fun clear() {
        HashSet(this.plots.keys).forEach { this.remove(it) }
    }

    @ProvidesNames(PLOT_TARGET)
    fun list(): Stream<String> {
        return stream().map<Name> { it.key }.map { it.toString() }
    }

    /**
     * Stream of all plots excluding intermediate nodes
     *
     * @return
     */
    fun stream(): Stream<Pair<Name, Plottable>> {
        return plots.values.stream().flatMap { pl ->
            if (pl is PlotGroup) {
                pl.stream().map { pair -> Pair<Name, Plottable>(Name.join(pl.name, pair.key), pair.value) }
            } else {
                Stream.of<Pair<Name, Plottable>>(Pair(pl.name, pl))
            }
        }
    }

    @Provides(PLOT_TARGET)
    fun opt(name: String): Optional<Plottable> {
        return opt(Name.of(name))
    }

    fun has(name: String): Boolean {
        return opt(name).isPresent
    }

    fun opt(name: Name): Optional<Plottable> {
        return if (name.length == 0) {
            throw RuntimeException("Zero length names are not allowed")
        } else if (name.length == 1) {
            Optional.ofNullable(plots[name])
        } else {
            opt(name.cutLast()).flatMap { plot ->
                if (plot is PlotGroup) {
                    plot.opt(name.last)
                } else {
                    Optional.empty()
                }
            }
        }
    }

    /**
     * Add plottable state listener
     *
     * @param listener
     */
    override fun addListener(listener: PlotListener) {
        listeners.add(listener)
    }

    /**
     * Remove plottable state listener
     *
     * @param listener
     */
    override fun removeListener(listener: PlotListener) {
        listeners.remove(listener)
    }

    /**
     * Notify that config for this element and children is changed
     */
    private fun notifyConfigChanged() {
        metaChanged(Name.EMPTY, this, Laminate(config).withDescriptor(descriptor))
        children.forEach { pl ->
            if (pl is PlotGroup) {
                pl.notifyConfigChanged()
            } else {
                metaChanged(pl.name, pl, Laminate(pl.config).withDescriptor(pl.descriptor))
            }
        }
    }

    override fun applyConfig(config: Meta) {
        super.applyConfig(config)
        notifyConfigChanged()
    }

    override fun getDescriptor(): NodeDescriptor {
        return descriptor
    }

    fun setDescriptor(descriptor: NodeDescriptor) {
        this.descriptor = descriptor
        notifyConfigChanged()
    }

    /**
     * Iterate over direct descendants
     *
     * @return
     */
    override fun iterator(): Iterator<Plottable> {
        return this.plots.values.iterator()
    }

    class Wrapper : hep.dataforge.io.envelopes.Wrapper<PlotGroup> {

        override val name: String
            get() = PLOT_GROUP_WRAPPER_TYPE

        override val type: Class<PlotGroup>
            get() = PlotGroup::class.java

        override fun wrap(obj: PlotGroup): Envelope {
            val baos = ByteArrayOutputStream()
            val writer = DefaultEnvelopeType.INSTANCE.writer

            for (plot in obj.plots.values) {
                try {
                    val env: Envelope
                    if (plot is PlotGroup) {
                        env = wrap(plot)
                    } else if (plot is Plot) {
                        env = plotWrapper.wrap(plot)
                    } else {
                        throw RuntimeException("Unknown plottable type")
                    }
                    writer.write(baos, env)
                } catch (ex: IOException) {
                    throw RuntimeException("Failed to write plot group to envelope", ex)
                }

            }

            val builder = EnvelopeBuilder()
                    .setMetaValue(WRAPPER_TYPE_KEY, PLOT_GROUP_WRAPPER_TYPE)
                    .setMetaValue("name", obj.name)
                    .putMetaNode(DEFAULT_META_NAME, obj.config)
                    .setContentType("wrapper")
                    .setData(baos.toByteArray())

            builder.putMetaNode("descriptor", obj.getDescriptor().toMeta())
            return builder.build()
        }

        override fun unWrap(envelope: Envelope): PlotGroup {
            //checkValidEnvelope(envelope);
            val groupName = envelope.meta.getString("name")
            val groupMeta = envelope.meta.getMetaOrEmpty(DEFAULT_META_NAME)
            val group = PlotGroup(groupName)
            group.configure(groupMeta)

            val internalEnvelopeType = EnvelopeType.resolve(envelope.meta.getString("@envelope.internalType", "default"))

            try {
                val dataStream = envelope.data.stream

                while (dataStream.available() > 0) {
                    val item = internalEnvelopeType!!.reader.read(dataStream)
                    try {
                        val pl = Plottable::class.java.cast(hep.dataforge.io.envelopes.Wrapper.unwrap(item))
                        group.add(pl)
                    } catch (ex: Exception) {
                        LoggerFactory.getLogger(javaClass).error("Failed to unwrap plottable", ex)
                    }

                }

                return group
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }

        }

        companion object {
            val PLOT_GROUP_WRAPPER_TYPE = "df.plots.group"
            private val plotWrapper = Plot.Wrapper()
        }
    }

    companion object {
        const val PLOT_TARGET = "plot"

        val WRAPPER = Wrapper()
    }
}
