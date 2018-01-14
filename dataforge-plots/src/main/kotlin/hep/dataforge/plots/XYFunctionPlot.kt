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
import hep.dataforge.description.ValueDefs
import hep.dataforge.kodex.intValue
import hep.dataforge.kodex.toList
import hep.dataforge.meta.Meta
import hep.dataforge.plots.data.XYPlot
import hep.dataforge.tables.Adapters.DEFAULT_XY_ADAPTER
import hep.dataforge.tables.Adapters.buildXYDataPoint
import hep.dataforge.values.ValueType.BOOLEAN
import hep.dataforge.values.Values
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import java.util.*
import java.util.function.Function

/**
 * A class for dynamic function values calculation for plot
 *
 * @author Alexander Nozik
 */
@ValueDefs(
        ValueDef(name = "showLine", type = arrayOf(BOOLEAN), def = "true", info = "Show the connecting line."),
        ValueDef(name = "showSymbol", type = arrayOf(BOOLEAN), def = "false", info = "Show symbols for data point."),
        ValueDef(name = "showErrors", type = arrayOf(BOOLEAN), def = "false", info = "Show errors for points.")
)
class XYFunctionPlot(name: String) : XYPlot(name) {

    private val cache = TreeMap<Double, Double>()
    private val function = SimpleObjectProperty<Function<Double, Double>>()
    private val lo = SimpleDoubleProperty()
    private val hi = SimpleDoubleProperty()

    var density by intValue()


    /**
     * The minimal number of points per range
     */
    //private val density = SimpleIntegerProperty(DEFAULT_DENSITY)

    init {
        //        getConfig().setValue("showLine", true);
        //        getConfig().setValue("showSymbol", false);
        function.addListener { _, _, _ ->
            invalidateCache()
        }
    }


    fun setFunction(function: Function<Double, Double>) {
        this.function.set(function)
    }

    /**
     * Turns line smoothing on or off
     *
     * @param smoothing
     */
    fun setSmoothing(smoothing: Boolean) {
        if (smoothing) {
            config.setValue("connectionType", "spline")
        } else {
            config.setValue("connectionType", "default")
        }
    }

    /**
     * @param from   lower range boundary
     * @param to     upper range boundary
     * @param notify notify listeners
     */
    fun setXRange(from: Double, to: Double, notify: Boolean) {
        lo.set(from)
        hi.set(to)
        if (notify) {
            super.notifyDataChanged()
        }
    }

    /**
     * Set minimum number of nodes per range
     *
     * @param density
     * @param notify
     */
    fun setDensity(density: Int, notify: Boolean) {
        this.density = density
        if (notify) {
            super.notifyDataChanged()
        }
    }

    /**
     * Split region into uniform blocks, then check if each block contains at
     * least one cached point and calculate additional point in the center of
     * the block if it does not.
     *
     *
     * If function is not set or desired density not positive does nothing.
     */
    private fun validateCache() {
        if (function.get() == null && density > 0) {
            //do nothing if there is no function
            return
        }
        // recalculate cache if boundaries are finite, otherwise use existing cache
        val from = lo.get()
        val to = hi.get()
        val nodes = this.density.toInt()
        if (java.lang.Double.isFinite(from) && java.lang.Double.isFinite(to)) {
            for (i in 0 until nodes) {
                val blockBegin = from + i * (to - from) / (nodes - 1)
                val blockEnd = from + (i + 1) * (to - from) / (nodes - 1)
                if (cache.subMap(blockBegin, blockEnd).isEmpty()) {
                    eval((blockBegin + blockEnd) / 2)
                }
            }
        }
    }

    @Synchronized private fun invalidateCache() {
        this.cache.clear()
    }

    /**
     * Calculate function cache for the given point and return calculated value
     *
     * @param x
     */
    @Synchronized protected fun eval(x: Double): Double {
        val y = function.get().apply(x)
        this.cache.put(x, y)
        return y
    }

    /**
     * Give the fixed point in which this function must be calculated. Calculate value and update range if it does not include point
     *
     * @param x
     */
    fun calculateIn(x: Double): Double {
        if (this.lo.value == null || this.lo.get() > x) {
            this.lo.set(x)
        }
        if (this.hi.value == null || this.hi.get() < x) {
            this.hi.set(x)
        }
        return eval(x)
    }

    override fun getRawData(query: Meta): List<Values> {
        //recalculate cache with default values
        if (query.hasValue("xRange.from")) {
            this.lo.set(query.getDouble("xRange.from"))
        }
        if (query.hasValue("xRange.to")) {
            this.hi.set(query.getDouble("xRange.to"))
        }
        if (query.hasValue("density")) {
            this.density = query.getInt("density")
        }
        validateCache()
        return cache.entries.stream()
                .map { entry -> buildXYDataPoint(DEFAULT_XY_ADAPTER, entry.key, entry.value) }
                .toList()
    }

    companion object {

        private val DEFAULT_DENSITY = 200

        fun plot(name: String, from: Double, to: Double, numPoints: Int = 200, function: (Double)-> Double): XYFunctionPlot {
            val p = XYFunctionPlot(name)
            p.setFunction(Function(function))
            p.setXRange(from, to, false)
            p.setDensity(numPoints, false)
            return p
        }
    }


}
