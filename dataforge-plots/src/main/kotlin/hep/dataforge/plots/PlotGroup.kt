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

import hep.dataforge.description.Descriptors
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.io.envelopes.DefaultEnvelopeType
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.io.envelopes.EnvelopeType
import hep.dataforge.io.envelopes.JavaObjectWrapper.JAVA_SERIAL_DATA
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
class PlotGroup(name: String, private var descriptor: NodeDescriptor = NodeDescriptor.empty("group"))
    : SimpleConfigurable(), Plottable, Provider, PlotListener, Iterable<Plottable> {

    override val name: Name = Name.ofSingle(name)

    private val plots = HashMap<Name, Plottable>()
    private val listeners = ReferenceRegistry<PlotListener>()

    val children: Collection<Plottable>
        get() = plots.values


//    constructor(name: String) {
//        this.name = Name.ofSingle(name)
//    }

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
        metaChanged(plot.name, plot, Laminate(plot.config).withDescriptor(Descriptors.forType(plot::class)))
        if (plot is PlotGroup) {
            plot.children.forEach { plot.notifyPlotAdded(it) }
        }
    }

    fun add(plot: Plottable) {
        synchronized(plots) {
            this.plots[plot.name] = plot
            plot.addListener(this)

            notifyPlotAdded(plot)
        }
    }

    operator fun Plottable.unaryPlus(){
        this@PlotGroup.add(this)
    }

    /**
     * Recursively create plot groups using given name
     */
    private fun createGroup(name: Name): PlotGroup {
        return if (name.isEmpty) {
            this
        } else {
            synchronized(this) {
                val subGroup = get(name.first) as? PlotGroup ?: PlotGroup(name.first.toString()).also { add(it) }
                subGroup.createGroup(name.cutFirst())
            }
        }
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
            (get(name.cutLast()) as? PlotGroup)?.remove(name.last)
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
     * Recursive stream of all plots excluding intermediate nodes
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
    operator fun get(name: String): Plottable? {
        return get(Name.of(name))
    }

    operator fun get(name: Name): Plottable? {
        return when {
            name.length == 0 -> throw RuntimeException("Zero length names are not allowed")
            name.length == 1 -> plots[name]
            else -> (get(name.cutLast()) as? PlotGroup)?.get(name.last)
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

    fun setType(type: Class<out Plottable>) {
        setDescriptor(Descriptors.forType(type.kotlin))
        configureValue("@descriptor", "class::${type.name}")
    }

    inline fun <reified T: Plottable> setType() {
        setType(T::class.java)
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
                    val env: Envelope = when (plot) {
                        is PlotGroup -> wrap(plot)
                        is Plot -> plotWrapper.wrap(plot)
                        else -> throw RuntimeException("Unknown plottable type")
                    }
                    writer.write(baos, env)
                } catch (ex: IOException) {
                    throw RuntimeException("Failed to write plot group to envelope", ex)
                }

            }

            val builder = EnvelopeBuilder()
                    .setMetaValue("name", obj.name)
                    .putMetaNode(DEFAULT_META_NAME, obj.config)
                    .setEnvelopeType(PLOT_GROUP_WRAPPER_TYPE)
                    .setDataType(JAVA_SERIAL_DATA)
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
            const val PLOT_GROUP_WRAPPER_TYPE = "hep.dataforge.plots.group"
            private val plotWrapper = Plot.Wrapper()
        }
    }

    companion object {
        const val PLOT_TARGET = "plot"

        inline fun <reified T : Plottable> typed(name: String): PlotGroup {
            return PlotGroup(name, Descriptors.forType(T::class))
        }

        val WRAPPER = Wrapper()
    }

}