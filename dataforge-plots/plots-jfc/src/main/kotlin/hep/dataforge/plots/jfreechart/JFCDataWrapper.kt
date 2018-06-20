/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.jfreechart

import hep.dataforge.meta.Meta
import hep.dataforge.plots.Plot
import hep.dataforge.tables.Adapters
import hep.dataforge.tables.ValuesAdapter
import hep.dataforge.values.Value
import hep.dataforge.values.Values
import org.jfree.data.xy.AbstractIntervalXYDataset

/**
 * Wrapper for plot. Multiple xs are not allowed
 *
 * @author Alexander Nozik
 */
internal class JFCDataWrapper(private var plot: Plot, private val query: Meta = Meta.empty()) : AbstractIntervalXYDataset() {

    private val adapter: ValuesAdapter = plot.adapter

    private var _data: List<Values>? = null

    private val data: List<Values>
        get() = _data ?: plot.getData(query).also { _data = it }

    var index: Int = 0


    fun getPlot(): Plot? {
        return plot
    }

    fun setPlot(plot: Plot) {
        if (this.plot !== plot) {
            this.plot = plot
            invalidateData()
        }
    }


    @Synchronized
    private fun getAt(i: Int): Values {
        return data[i]
    }

    override fun getSeriesCount(): Int {
        return 1
    }

    override fun getSeriesKey(i: Int): Comparable<*> {
        return if (seriesCount == 1) {
            plot.name.toUnescaped()
        } else {
            plot.name.append(Adapters.getTitle(adapter, Adapters.Y_AXIS)).toUnescaped()
        }
    }

    override fun getStartX(i: Int, i1: Int): Number {
        return Adapters.getLowerBound(adapter, Adapters.X_AXIS, getAt(i1))
    }

    override fun getEndX(i: Int, i1: Int): Number {
        return Adapters.getUpperBound(adapter, Adapters.X_AXIS, getAt(i1))
    }

    override fun getStartY(i: Int, i1: Int): Number {
        return Adapters.getLowerBound(adapter, Adapters.Y_AXIS, getAt(i1))
    }

    override fun getEndY(i: Int, i1: Int): Number {
        return Adapters.getUpperBound(adapter, Adapters.Y_AXIS, getAt(i1))
    }

    override fun getItemCount(i: Int): Int {
        return data.size
    }

    override fun getX(i: Int, i1: Int): Number? {
        return transform(Adapters.getXValue(adapter, getAt(i1)))
    }


    override fun getY(i: Int, i1: Int): Number? {
        return transform(Adapters.getYValue(adapter, getAt(i1)))
    }

    private fun transform(value: Value): Number? {
        return if (value.isNull) {
            null
        } else {
            value.double
        }
    }


    fun invalidateData() {
        this._data = null
    }
}
