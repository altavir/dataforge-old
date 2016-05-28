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
package hep.dataforge.plots.data;

import hep.dataforge.io.envelopes.EnvelopeBuilder;
//import hep.dataforge.maths.GridCalculator;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.MapPoint;
import hep.dataforge.tables.XYAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Alexander Nozik
 */
public class PlottableFunction extends XYPlottable {

    private final List<Double> grid;
    private final Function<Double, Double> function;
    //TODO Сделать построение графика по заданной решетке

    public PlottableFunction(String name, Function<Double, Double> function, double from, double to, int numPoints) {
        super(name);
        getConfig().setValue("showLine", true);
        getConfig().setValue("showSymbol", false);
        this.function = function;

        grid = new ArrayList<>(numPoints);

        for (int i = 0; i < numPoints; i++) {
            grid.add(from + i * (to - from) / (numPoints - 1));
        }

    }

    /**
     * Build function calculated in given data nodes
     *
     * @param name
     * @param meta
     * @param function
     * @param data
     * @param xName
     */
    public PlottableFunction(String name, Function<Double, Double> function, Iterable<DataPoint> data, XYAdapter adapter) {
        super(name);
        getConfig().setValue("showLine", true);
        getConfig().setValue("showSymbol", false);
        this.function = function;

        List<Double> tempGrid = new ArrayList<>();
        data.forEach((dp) -> tempGrid.add(adapter.getX(dp).doubleValue()));
        Collections.sort(tempGrid);

        grid = new ArrayList<>();
        for (int i = 0; i < tempGrid.size() - 1; i++) {
            grid.add(tempGrid.get(i));
            grid.add((tempGrid.get(i) + tempGrid.get(i + 1)) / 2);
        }
        grid.add(tempGrid.get(tempGrid.size() - 1));
    }

    @Override
    public Stream<DataPoint> dataStream(Meta dataConfiguration) {
        //TODO use information from cfg
        return filterDataStream(grid.stream().map(x -> new MapPoint(new String[]{"x", "y"}, x, function.apply(x))), dataConfiguration);
    }

    @Override
    protected EnvelopeBuilder wrapBuilder() {
        return super.wrapBuilder().putMetaValue("showErrors", false);
    }

}
