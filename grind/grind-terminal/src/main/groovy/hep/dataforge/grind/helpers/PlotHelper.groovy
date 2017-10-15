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
import hep.dataforge.grind.Grind
import hep.dataforge.grind.GrindMetaBuilder
import hep.dataforge.io.markup.MarkupBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.plots.PlotPlugin
import hep.dataforge.plots.data.DataPlot
import hep.dataforge.plots.data.XYFunctionPlot
import hep.dataforge.plots.data.XYPlot
import hep.dataforge.tables.ValuesAdapter
import hep.dataforge.tables.XYAdapter
import hep.dataforge.values.ValueType
import hep.dataforge.values.Values

import java.util.function.Function

/**
 * Created by darksnake on 30-Aug-16.
 */
class PlotHelper extends AbstractHelper {
    static final String DEFAULT_FRAME = "default";

    PlotPlugin manager;

    PlotHelper(Context context = Global.instance()) {
        super(context)
        context.pluginManager().getOrLoad("plots")
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
     * Plot function and return resulting plot to be configured if necessary
     * @param parameters
     * @param function
     */
    @MethodDescription("Plot a function defined by a closure.")
    @ValueDefs([
            @ValueDef(name = "frame", info = "Frame name"),
            @ValueDef(name = "name", info = "Plot name"),
            @ValueDef(name = "from", type = ValueType.NUMBER, def = "0", info = "Lower x boundary for plot"),
            @ValueDef(name = "to", type = ValueType.NUMBER, def = "1", info = "Upper x boundary for plot"),
            @ValueDef(name = "numPoints", type = ValueType.NUMBER, def = "100", info = "Number of points per plot")
    ])
    XYPlot plot(Map parameters = [:], Closure<Double> function) {
        String frameName = parameters.get("frame", DEFAULT_FRAME)
        String pltName = parameters.get("name", "function_${function.hashCode()}");
        double from = parameters.get("from", 0d) as Double;
        double to = parameters.get("to", 1d) as Double;
        int numPoints = parameters.get("numPoints", 100) as Integer;
        Function<Double, Double> func = { Double x -> function.call(x) as Double } as Function
        XYFunctionPlot res = XYFunctionPlot.plotFunction(pltName, func, from, to, numPoints);
        res.configure(parameters)
        manager.getPlotFrame(frameName).add(res)
        return res;
    }


    @MethodDescription("Plot data using x and y array")
    XYPlot plot(double[] x, double[] y, String name = "data", String frame = DEFAULT_FRAME) {
        def res = DataPlot.plot(name, x, y);
        manager.getPlotFrame(frame).add(res)
        return res;
    }

    XYPlot plot(List x, List y, String name = "data", String frame = DEFAULT_FRAME) {
        def res = DataPlot.plot(name, x as double[], y as double[]);
        manager.getPlotFrame(frame).add(res)
        return res;
    }

    @MethodDescription("Plot data using x-y map")
    XYPlot plot(Map<Number, Number> dataMap, String name = "data", String frame = DEFAULT_FRAME) {
        def x = [];
        def y = [];
        dataMap.forEach { k, v ->
            x << (k as double)
            y << (v as double)
        }
        return plot(x, y, name, frame);
    }

    @MethodDescription("Plot data using iterable point source and adapter")
    XYPlot plot(Iterable<Values> source, ValuesAdapter adapter = XYAdapter.DEFAULT_ADAPTER, String name = "data", String frame = DEFAULT_FRAME) {
        def res = DataPlot.plot(name, XYAdapter.from(adapter), source);
        manager.getPlotFrame(frame).add(res)
        return res;
    }

    /**
     * Build data plot using any point source and closure to configure adapter
     * @param source
     * @param parameters
     * @param cl
     * @return
     */
    @MethodDescription("Build data plot using any point source and closure to configure adapter")
    XYPlot plot(Map parameters, Iterable<Values> source,
                @DelegatesTo(GrindMetaBuilder) Closure cl = null) {
        Meta configuration = Grind.buildMeta(parameters, cl);
        String name = configuration.getString("name", "data_${source.hashCode()}")
        String frameName = configuration.getString("frame", DEFAULT_FRAME)
        def res = new DataPlot(name, configuration);

        res.fillData(source);

        manager.getPlotFrame(frameName).add(res)
        return res;
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
