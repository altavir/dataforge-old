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

import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.PlottableData;
import hep.dataforge.plots.data.PlottableXYFunction;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
import hep.dataforge.tables.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Alexander Nozik
 */
public class JFreeFXTest extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        AnchorPane root = new AnchorPane();

        JFreeChartFrame frame = new JFreeChartFrame();
        frame.display(root);

        Function<Double, Double> func = (x1) -> x1 * x1;

        PlottableXYFunction funcPlot = PlottableXYFunction.plotFunction("func", func, 0.1, 4, 200);

        frame.add(funcPlot);

        String[] names = {"myX", "myY", "myXErr", "myYErr"};

        List<DataPoint> data = new ArrayList<>();
        data.add(new MapPoint(names, 0.5d, 0.2, 0.1, 0.1));
        data.add(new MapPoint(names, 1d, 1d, 0.2, 0.5));
        data.add(new MapPoint(names, 3d, 7d, 0, 0.5));
        Table ds = new ListTable(data);

        PlottableData dataPlot = PlottableData.plot("dataPlot", new XYAdapter("myX", "myY", "myXErr", "myYErr"), ds);

        frame.getConfig().putNode(new MetaBuilder("yAxis").putValue("logScale", true));

        frame.add(dataPlot);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("my plot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
