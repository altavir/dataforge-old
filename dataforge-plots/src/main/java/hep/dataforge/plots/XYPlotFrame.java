/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.plots;

import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.data.XYPlottable;
import hep.dataforge.tables.XYAdapter;

/**
 * @author Alexander Nozik
 */
@NodeDef(name = "xAxis", info = "The description of X axis", target = "method::hep.dataforge.plots.XYPlotFrame.updateAxis")
@NodeDef(name = "yAxis", info = "The description of Y axis", target = "method::hep.dataforge.plots.XYPlotFrame.updateAxis")
@NodeDef(name = "legend", info = "The configuration for plot legend", target = "method::hep.dataforge.plots.XYPlotFrame.updateLegend")
public abstract class XYPlotFrame extends AbstractPlotFrame<XYPlottable> {


    @Override
    protected abstract void updatePlotData(String name);

    @Override
    protected abstract void updatePlotConfig(String name);

    @Override
    protected void applyConfig(Meta config) {
        if (config == null) {
            return;
        }

        updateFrame(config);
        //Вызываем эти методы, чтобы не делать двойного обновления аннотаций
        updateAxis(XYAdapter.X_AXIS, getXAxisConfig(), getConfig());

        updateAxis(XYAdapter.Y_AXIS, getYAxisConfig(), getConfig());

        updateLegend(getLegendConfig());

    }

    protected abstract void updateFrame(Meta annotation);

    public Configuration getXAxisConfig() {
        return getConfig().requestNode("xAxis");
    }

    public Configuration getYAxisConfig() {
        return getConfig().requestNode("yAxis");
    }

    public Configuration getLegendConfig() {
        return getConfig().requestNode("legend");
    }

    /**
     * перерисовка осей
     *
     * @param axisName
     * @param axisMeta
     */
    @ValueDef(name = "type", allowed = "[number, log, time]", def = "number",
            info = "The type of axis. By default number axis is used")
    @ValueDef(name = "axisTitle", info = "The title of the axis.")
    @ValueDef(name = "axisUnits", def = "", info = "The units of the axis.")
    @ValueDef(name = "crosshair", def = "none",
            allowed = "[none, free, data]",
            info = "Appearance and type of crosshair")
    protected abstract void updateAxis(String axisName, Meta axisMeta, Meta plotMeta);

    @ValueDef(name = "show", type = "BOOLEAN", def = "true", info = "Display or hide the legend")
    protected abstract void updateLegend(Meta legendMeta);
}
