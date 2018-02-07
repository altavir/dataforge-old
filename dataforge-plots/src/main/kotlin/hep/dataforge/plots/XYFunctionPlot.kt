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
import hep.dataforge.kodex.*
import hep.dataforge.meta.Meta
import hep.dataforge.plots.data.XYPlot
import hep.dataforge.tables.Adapters.DEFAULT_XY_ADAPTER
import hep.dataforge.tables.Adapters.buildXYDataPoint
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType.BOOLEAN
import hep.dataforge.values.ValueType.NUMBER
import hep.dataforge.values.Values
import java.util.*

/**
 * A class for dynamic function values calculation for plot
 *
 * @author Alexander Nozik
 */
@ValueDefs(
        ValueDef(name = "showLine", type = arrayOf(BOOLEAN), def = "true", info = "Show the connecting line."),
        ValueDef(name = "showSymbol", type = arrayOf(BOOLEAN), def = "false", info = "Show symbols for data point."),
        ValueDef(name = "showErrors", type = arrayOf(BOOLEAN), def = "false", info = "Show errors for points."),
        ValueDef(name = "range.from", type = arrayOf(NUMBER), def = "0.0", info = "Lower boundary for calculation range"),
        ValueDef(name = "range.to", type = arrayOf(NUMBER), def = "1.0", info = "Upper boundary for calculation range"),
        ValueDef(name = "density", type = arrayOf(NUMBER), def = "200", info = "Minimal number of points per plot"),
        ValueDef(name = "connectionType", def = "spline")
)
class XYFunctionPlot(name: String, val function: (Double) -> Double) : XYPlot(name) {

    private val cache = TreeMap<Double, Double>()

    /**
     * The minimal number of points per range
     */

    var density by intValue()
    var from by doubleValue("range.from")
    var to by doubleValue("range.to")

    /**
     * Turns line smoothing on or off
     *
     * @param smoothing
     */
    var smoothing by customValue("connectionType", read = { it.stringValue() == "spline" }) {
        if (it) {
            "spline"
        } else {
            "default"
        }
    }

    var range by customNode("range", read = { Pair(it.getDouble("from"), it.getDouble("to")) }) {
        invalidateCache()
        buildMeta("range", "from" to it.first, "to" to it.second)
    }

    override fun applyValueChange(name: String, oldItem: Value?, newItem: Value?) {
        super.applyValueChange(name, oldItem, newItem)
        if (name == "density") {
            invalidateCache()
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
        // recalculate immutable if boundaries are finite, otherwise use existing immutable
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

    @Synchronized
    private fun invalidateCache() {
        this.cache.clear()
    }

    /**
     * Calculate function immutable for the given point and return calculated value
     *
     * @param x
     */
    @Synchronized
    private fun eval(x: Double): Double {
        val y = function(x)
        this.cache.put(x, y)
        return y
    }

    /**
     * Give the fixed point in which this function must be calculated. Calculate value and update range if it does not include point
     *
     * @param x
     */
    fun calculateIn(x: Double): Double {
        if (this.from > x) {
            this.from = x
        }
        if (this.to < x) {
            this.to = x
        }
        return eval(x)
    }

    override fun getRawData(query: Meta): List<Values> {
        //recalculate immutable with default values
        if (query.hasValue("xRange.from")) {
            this.from = query.getDouble("xRange.from")
        }
        if (query.hasValue("xRange.to")) {
            this.to = query.getDouble("xRange.to")
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

        private const val DEFAULT_DENSITY = 200

        @JvmOverloads
        fun plot(name: String, from: Double, to: Double, numPoints: Int = DEFAULT_DENSITY, function: (Double) -> Double): XYFunctionPlot {
            val p = XYFunctionPlot(name, function)
            p.range = Pair(from, to)
            p.density = numPoints
            return p
        }
    }


}
