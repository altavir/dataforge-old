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

package hep.dataforge.grind.helpers

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.io.markup.MarkupBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.plots.PlotPlugin
import hep.dataforge.plots.data.DataPlot
import hep.dataforge.plots.data.XYFunctionPlot
import hep.dataforge.plots.data.XYPlot
import hep.dataforge.tables.Adapters
import hep.dataforge.tables.ValuesAdapter
import hep.dataforge.values.ValueType

import java.util.function.Function

/**
 * Created by darksnake on 30-Aug-16.
 */
class PlotHelper extends AbstractHelper {
    static final String DEFAULT_FRAME = "default";

    PlotPlugin manager;

    PlotHelper(Context context = Global.instance()) {
        super(context)
        context.getPluginManager().getOrLoad("plots")
        this.manager = context.getFeature(PlotPlugin)
    }

    PlotPlugin getManager() {
        return manager
    }


    def configure(String frame, Closure config) {
        manager.getPlotFrame(frame).configure(config);
    }

    def configure(Closure config) {
        manager.getPlotFrame(DEFAULT_FRAME).configure(config);
    }

    @MethodDescription("Apply meta to frame with given name")
    def configure(String frame, Map values, Closure config) {
        manager.getPlotFrame(frame).configure(values, config);
    }

    @MethodDescription("Apply meta to default frame")
    def configure(Map values, Closure config) {
        manager.getPlotFrame(DEFAULT_FRAME).configure(values, config);
    }

    /**
     * Plot function and return resulting frame to be configured if necessary
     * @param parameters
     * @param function
     */
    @MethodDescription("Plot a function defined by a closure.")
    @ValueDefs([
            @ValueDef(name = "frame", info = "Frame name"),
            @ValueDef(name = "name", info = "Plot name"),
            @ValueDef(name = "from", type = ValueType.NUMBER, def = "0", info = "Lower x boundary for frame"),
            @ValueDef(name = "to", type = ValueType.NUMBER, def = "1", info = "Upper x boundary for frame"),
            @ValueDef(name = "numPoints", type = ValueType.NUMBER, def = "100", info = "Number of points per frame")
    ])
    XYPlot plotFunction(double from = 0d, double to = 1d, int numPoints = 100, String name = "data", String frame = DEFAULT_FRAME, Closure<Double> function) {
        Function<Double, Double> func = { Double x -> function.call(x) as Double }
        XYFunctionPlot res = XYFunctionPlot.plotFunction(name, func, from, to, numPoints);
        manager.getPlotFrame(frame).add(res)
        return res;
    }

    private XYPlot buildDataPlot(Map map) {
        DataPlot plot = new DataPlot(map.getOrDefault("name", "data") as String, map.getOrDefault("meta", Meta.empty()) as Meta)
        if (map["adapter"]) {
            plot.setAdapter(map["adapter"] as ValuesAdapter)
        } else {
            plot.setAdapter(Adapters.DEFAULT_XY_ADAPTER)
        }
        if (map["data"]) {
            def data = map.data
            if (data instanceof Map) {
                data.forEach { k, v ->
                    plot.append(k as Number, v as Number)
                }
            } else if (data instanceof Iterable) {
                plot.fillData(data)
            } else {
                throw new RuntimeException("Unrecognized data type: ${data.class}")
            }
        } else if (map["x"] && map["y"]) {
            def x = map["x"] as List
            def y = map["y"] as List
            [x, y].transpose().each { List it ->
                plot.append(it[0] as Number, it[1] as Number)
            }
        }
        return plot;
    }

    @MethodDescription("Plot data using supplied parameters")
    XYPlot plot(Map parameters) {
        def res = buildDataPlot(parameters)
        manager.getPlotFrame(parameters.getOrDefault("frame", DEFAULT_FRAME) as String).add(res)
        return res
    }

    @Override
    Context getContext() {
        return context;
    }

    @Override
    protected MarkupBuilder getHelperDescription() {
        return new MarkupBuilder().text("This is ").text("plots", "blue").text(" helper");
    }

}
