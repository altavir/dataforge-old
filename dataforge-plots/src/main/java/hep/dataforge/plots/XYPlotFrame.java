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

/**
 *
 * @author Alexander Nozik
 */
@NodeDef(name = "xAxis", info = "The description of X axis", target = "method::hep.dataforge.plots.XYPlotFrame.updateAxis")
@NodeDef(name = "yAxis", info = "The description of Y axis", target = "method::hep.dataforge.plots.XYPlotFrame.updateAxis")
@NodeDef(name = "legend", info = "The configuration for plot legend")
public abstract class XYPlotFrame extends AbstractPlotFrame<XYPlottable> {

    public XYPlotFrame(String name, Meta annotation) {
        super(name, annotation);
    }

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
        if (config.hasNode("xAxis")) {
            updateAxis("x", config.getNode("xAxis"));
        }
        if (config.hasNode("yAxis")) {
            updateAxis("y", config.getNode("yAxis"));
        }

    }

    protected abstract void updateFrame(Meta annotation);

    public Configuration getXAxisConfig() {
        return getConfig().getNode("xAxis", new Configuration("xAxis"));
    }

    public Configuration getYAxisConfig() {
        return getConfig().getNode("yAxis", new Configuration("yAxis"));
    }

    /**
     * перерисовка осей
     *
     * @param axisName
     * @param annotation
     */
    @ValueDef(name = "timeAxis", type = "BOOLEAN", def = "false", info = "Time axis.")
    @ValueDef(name = "logScale", type = "BOOLEAN", def = "false", info = "Apply logariphm scale to axis.")
    @ValueDef(name = "axisTitle", info = "The title of the axis.")
    @ValueDef(name = "axisUnits", def = "", info = "The units of the axis.")
    protected abstract void updateAxis(String axisName, Meta annotation);

//    protected abstract void updateXYPlot(XYPlottable plottable);
}
