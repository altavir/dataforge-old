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

import hep.dataforge.plots.fx.FXTimeAxis;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Darksnake
 */
public class TestFXAxis extends Application {

    Timer timer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FX axis test");
        //defining the axes
        final FXTimeAxis xAxis = new FXTimeAxis();
        final NumberAxis yAxis = new NumberAxis();
        //creating the chart
        final LineChart<Instant,Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);

        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("test");

        ToggleButton button = new ToggleButton("Start/Stop");
        button.setOnAction((ActionEvent event) -> {
            togglePlot(series, button.isSelected());
        });

        VBox box = new VBox(10, lineChart, button);

        Scene scene = new Scene(box, 800, 600);
        lineChart.getData().add(series);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void togglePlot(XYChart.Series data, boolean toggle) {
        if (timer != null) {
            timer.cancel();
        }
        if (toggle) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    Instant time = Instant.now();
                    double x = ((double) time.toEpochMilli()) / 5000d * Math.PI;
                    Platform.runLater(() -> {
                        data.getData().
                                add(new XYChart.Data<>(time, 2.0 + Math.sin(x)));
                    });
                }
            }, 0, 1000);
        }
    }

}
