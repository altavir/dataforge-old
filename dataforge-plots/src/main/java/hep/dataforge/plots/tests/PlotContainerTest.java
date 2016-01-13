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
package hep.dataforge.plots.tests;

import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.ListDataSet;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.data.XYDataAdapter;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.PlottableData;
import hep.dataforge.plots.data.PlottableFunction;
import hep.dataforge.plots.fx.FXPlotUtils;
import hep.dataforge.plots.fx.PlotContainer;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 *
 * @author Alexander Nozik
 */
public class PlotContainerTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PlotContainer container = FXPlotUtils.displayContainer("My test container", 800, 600);

        JFreeChartFrame frame = new JFreeChartFrame("my plot", null);
        
        container.setPlot(frame);

        UnivariateFunction func = (double x1) -> x1 * x1;

        PlottableFunction funcPlot = new PlottableFunction("func", null, func, 0.1, 4, 200);

        frame.add(funcPlot);

        String[] names = {"myX", "myY", "myXErr", "myYErr"};

        List<DataPoint> data = new ArrayList<>();
        data.add(new MapDataPoint(names, 0.5d, 0.2, 0.1, 0.1));
        data.add(new MapDataPoint(names, 1d, 1d, 0.2, 0.5));
        data.add(new MapDataPoint(names, 3d, 7d, 0, 0.5));
        DataSet ds = new ListDataSet("data", null, data);

        PlottableData dataPlot = PlottableData.plot(ds, new XYDataAdapter("myX", "myXErr", "myY", "myYErr"));

        frame.getConfig().setNode(new MetaBuilder("yAxis").putValue("type", "log"));

        frame.add(dataPlot);
    }

}
