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
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.values.Value;

import java.util.Optional;

import static hep.dataforge.tables.Adapters.X_AXIS;
import static hep.dataforge.tables.Adapters.Y_AXIS;

/**
 * Two-axis plot frame
 *
 * @author Alexander Nozik
 */
@NodeDef(name = "xAxis", info = "The description of X axis", from = "method::hep.dataforge.plots.XYPlotFrame.updateAxis")
@NodeDef(name = "yAxis", info = "The description of Y axis", from = "method::hep.dataforge.plots.XYPlotFrame.updateAxis")
@NodeDef(name = "legend", info = "The configuration for plot legend", from = "method::hep.dataforge.plots.XYPlotFrame.updateLegend")
public abstract class XYPlotFrame extends AbstractPlotFrame {


    @Override
    protected void applyConfig(Meta config) {
        if (config == null) {
            return;
        }

        updateFrame(config);
        //Вызываем эти методы, чтобы не делать двойного обновления аннотаций
        updateAxis(X_AXIS, getXAxisConfig(), getConfig());

        updateAxis(Y_AXIS, getYAxisConfig(), getConfig());

        updateLegend(getLegendConfig());

    }

    protected abstract void updateFrame(Meta annotation);

    private Meta getXAxisConfig() {
        return getConfig().getMetaOrEmpty("xAxis");
    }

    private Meta getYAxisConfig() {
        return getConfig().getMetaOrEmpty("yAxis");
    }

    private Meta getLegendConfig() {
        return getConfig().getMetaOrEmpty("legend");
    }

    /**
     * перерисовка осей
     *
     * @param axisName
     * @param axisMeta
     */
    @NodeDef(name = "range", info = "The definition of range for given axis")
    protected abstract void updateAxis(String axisName, Meta axisMeta, Meta plotMeta);

    protected abstract void updateLegend(Meta legendMeta);

    /**
     * Get actual color value for displayed plot. Some color could be assigned even if it is missing from configuration
     *
     * @param name
     * @return
     */
    public Optional<Value> getActualColor(Name name) {
        return getPlots().opt(name).flatMap(plot -> plot.getConfig().optValue("color"));
    }
}
