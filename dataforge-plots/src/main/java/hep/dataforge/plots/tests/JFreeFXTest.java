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
import hep.dataforge.data.ListPointSet;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.data.XYAdapter;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.PlottableData;
import hep.dataforge.plots.data.PlottableFunction;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.math3.analysis.UnivariateFunction;
import hep.dataforge.data.PointSet;

/**
 *
 * @author Alexander Nozik
 */
public class JFreeFXTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        AnchorPane root = new AnchorPane();

        JFreeChartFrame frame = new JFreeChartFrame("my plot", null).display(root);

        UnivariateFunction func = (double x1) -> x1 * x1;

        PlottableFunction funcPlot = new PlottableFunction("func", func, 0.1, 4, 200);

        frame.add(funcPlot);

        String[] names = {"myX", "myY", "myXErr", "myYErr"};

        List<DataPoint> data = new ArrayList<>();
        data.add(new MapDataPoint(names, 0.5d, 0.2, 0.1, 0.1));
        data.add(new MapDataPoint(names, 1d, 1d, 0.2, 0.5));
        data.add(new MapDataPoint(names, 3d, 7d, 0, 0.5));
        PointSet ds = new ListPointSet("data", null, data);

        PlottableData dataPlot = PlottableData.plot(ds, new XYAdapter("myX", "myY", "myXErr", "myYErr"));

        frame.getConfig().putNode(new MetaBuilder("yAxis").putValue("logScale", true));

        frame.add(dataPlot);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("my plot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
