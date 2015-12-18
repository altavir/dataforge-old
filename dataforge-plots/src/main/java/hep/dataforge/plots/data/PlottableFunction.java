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

import hep.dataforge.meta.Meta;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.maths.GridCalculator;
import hep.dataforge.plots.XYPlottable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 *
 * @author Alexander Nozik
 */
public class PlottableFunction extends XYPlottable {

    private final List<Double> grid;
    private final UnivariateFunction function;
    //TODO Сделать построение графика по заданной решетке

    public PlottableFunction(String name, Meta annotation, UnivariateFunction function, double from, double to, int numPoints) {
        super(name, null);
        getConfig().setValue("showLine", true);
        getConfig().setValue("showSymbol", false);
        this.function = function;

        grid = new ArrayList<>(numPoints);
        for (double d : GridCalculator.getUniformUnivariateGrid(from, to, numPoints)) {
            grid.add(d);
        }

    }

    /**
     * Build function calculated in given data nodes
     * @param name
     * @param annotation
     * @param function
     * @param data
     * @param xName 
     */
    public PlottableFunction(String name, Meta annotation, UnivariateFunction function, Iterable<DataPoint> data, String xName) {
        super(name, null);
        getConfig().setValue("showLine", true);
        getConfig().setValue("showSymbol", false);
        this.function = function;

        List<Double> tempGrid = new ArrayList<>();
        data.forEach((dp) -> tempGrid.add(dp.getDouble(xName)));
        Collections.sort(tempGrid);

        grid = new ArrayList<>();
        for (int i = 0; i < tempGrid.size() - 1; i++) {
            grid.add(tempGrid.get(i));
            grid.add((tempGrid.get(i) + tempGrid.get(i + 1)) / 2);
        }
        grid.add(tempGrid.get(tempGrid.size() - 1));
    }

    @Override
    public Collection<DataPoint> plotData() {

        List<DataPoint> list = new ArrayList<>();

        for (double x : grid) {
            list.add(new MapDataPoint(new String[]{"x", "y"}, x, function.value(x)));
        }
        return list;
    }

}
