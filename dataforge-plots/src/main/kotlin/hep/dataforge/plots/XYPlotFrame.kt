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

import hep.dataforge.description.NodeDef
import hep.dataforge.description.NodeDefs
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.tables.Adapters.X_AXIS
import hep.dataforge.tables.Adapters.Y_AXIS
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import java.util.*

/**
 * Two-axis plot frame
 *
 * @author Alexander Nozik
 */
@NodeDefs(
        NodeDef(key = "xAxis", info = "The description of X axis", from = "method::hep.dataforge.plots.XYPlotFrame.updateAxis"),
        NodeDef(key = "yAxis", info = "The description of Y axis", from = "method::hep.dataforge.plots.XYPlotFrame.updateAxis"),
        NodeDef(key = "legend", info = "The configuration for plot legend", from = "method::hep.dataforge.plots.XYPlotFrame.updateLegend")
)
abstract class XYPlotFrame : AbstractPlotFrame() {

    private val xAxisConfig: Meta
        get() = config.getMetaOrEmpty("xAxis")

    private val yAxisConfig: Meta
        get() = config.getMetaOrEmpty("yAxis")

    private val legendConfig: Meta
        get() = config.getMetaOrEmpty("legend")


    override fun applyConfig(config: Meta?) {
        if (config == null) {
            return
        }

        updateFrame(config)
        //Вызываем эти методы, чтобы не делать двойного обновления аннотаций
        updateAxis(X_AXIS, xAxisConfig, getConfig())

        updateAxis(Y_AXIS, yAxisConfig, getConfig())

        updateLegend(legendConfig)

    }

    protected abstract fun updateFrame(annotation: Meta)

    //TODO replace axis type by enumeration

    /**
     * Configure axis
     *
     * @param axisName
     * @param axisMeta
     */
    @ValueDefs(
            ValueDef(key = "type", allowed = ["number", "log", "time"], def = "number", info = "The type of axis. By default number axis is used"),
            ValueDef(key = "axisTitle", info = "The title of the axis."),
            ValueDef(key = "axisUnits", def = "", info = "The units of the axis."),
            ValueDef(key = "range.from", type = arrayOf(ValueType.NUMBER), info = "Lower boundary for fixed range"),
            ValueDef(key = "range.to", type = arrayOf(ValueType.NUMBER), info = "Upper boundary for fixed range"),
            ValueDef(key = "crosshair", def = "data", allowed = ["none", "free", "data"], info = "Appearance and type of the crosshair")
    )
    @NodeDef(key = "range", info = "The definition of range for given axis")
    protected abstract fun updateAxis(axisName: String, axisMeta: Meta, plotMeta: Meta)

    @ValueDef(key = "show", type = arrayOf(ValueType.BOOLEAN), def = "true", info = "Display or hide the legend")
    protected abstract fun updateLegend(legendMeta: Meta)

    /**
     * Get actual color value for displayed plot. Some color could be assigned even if it is missing from configuration
     *
     * @param name
     * @return
     */
    open fun getActualColor(name: Name): Optional<Value> {
        return plots[name]?.config?.optValue("color") ?: Optional.empty()
    }
}
