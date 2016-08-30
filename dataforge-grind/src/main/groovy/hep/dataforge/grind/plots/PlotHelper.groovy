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

package hep.dataforge.grind.plots

import hep.dataforge.context.Context
import hep.dataforge.plots.PlotHolder
import hep.dataforge.plots.PlotsPlugin
import hep.dataforge.plots.XYPlottable
import hep.dataforge.plots.data.PlottableData
import hep.dataforge.plots.data.PlottableXYFunction
import hep.dataforge.tables.PointSource
import hep.dataforge.tables.XYAdapter

import java.util.function.Function

/**
 * Created by darksnake on 30-Aug-16.
 */
class PlotHelper {
    static final DEFAULT_FRAME = "default";
    PlotHolder holder;

    PlotHelper(Context context) {
        this.holder = PlotsPlugin.buildFrom(context);
    }


    def configure(String frame, Closure config) {
        holder.getPlotFrame(frame).configure { config };
    }

    def configure(Closure config) {
        holder.getPlotFrame(DEFAULT_FRAME).configure { config };
    }

    def configure(String frame, Map values, Closure config) {
        holder.getPlotFrame(frame).configure(values) { config };
    }

    def configure(Map values, Closure config) {
        holder.getPlotFrame(DEFAULT_FRAME).configure(values) { config };
    }

    /**
     * Plot function and return resulting plottable to be configured if necessary
     * @param parameters
     * @param function
     */
    XYPlottable plot(Map parameters, Function<Double, Double> function) {
        String frameName = parameters.get("frame", DEFAULT_FRAME)
        String pltName = parameters.get("name", "function");
        double from = parameters.get("from", 0d);
        double to = parameters.get("tu", 1d);
        double numPoints = parameters.get("numPoints", 100);
        def res = PlottableXYFunction.plotFunction(pltName, function, from, to, numPoints);
        holder.getPlotFrame(frameName).add(res)
        return res;
    }

    XYPlottable plot(double[] x, double[] y, String name = "data", String frame = DEFAULT_FRAME) {
        def res = PlottableData.plot(name, x, y);
        holder.getPlotFrame(frame).add(res)
        return res;
    }

    XYPlottable plot(List x, List y, String name = "data", String frame = DEFAULT_FRAME) {
        def res = PlottableData.plot(name, x as double[], y as double[]);
        holder.getPlotFrame(frame).add(res)
        return res;
    }

    XYPlottable plot(Map dataMap, String name = "data", String frame = DEFAULT_FRAME) {
        double[] x = [];
        double[] y = [];
        dataMap.forEach { k, v ->
            x << k
            y << v
        }
        return plot(x, y, pltName, frameName);
    }

    XYPlottable plot(PointSource source, XYAdapter adapter = XYAdapter.DEFAULT_ADAPTER, String name = "", String frame = DEFAULT_FRAME) {
        def res = PlottableData.plot(name, adapter, source);
        holder.getPlotFrame(frame).add(res)
        return res;
    }


}
