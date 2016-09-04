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
package hep.dataforge.plots.fx;

import hep.dataforge.meta.Meta;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.XYAdapter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotResult;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alexander Nozik
 */
public class FXLineChartFrame extends XYPlotFrame implements FXPlotFrame<XYPlottable> {

    LineChart<Number, Number> chart;

    /**
     * Вставить и растянуть на всю ширину
     */
    public FXLineChartFrame() {
        this.chart = new LineChart<>(new NumberAxis(), new NumberAxis());
    }

    XYChart.Series<Number, Number> getSeries(String name) {
        XYChart.Series<Number, Number> series = null;
        for (XYChart.Series ser : chart.getData()) {
            if (ser != null && name.equals(ser.getName())) {
                series = ser;
                break;
            }
        }
        return series;
    }

    @Override
    protected void updateAxis(String axisName, Meta annotation) {

    }

    @Override
    protected void updateFrame(Meta annotation) {

    }

    @Override
    protected void updatePlotData(String name) {
        XYPlottable plottable = get(name);
        XYChart.Series<Number, Number> series = getSeries(name);

        if (series == null) {
            series = new XYChart.Series<>();
            series.setName(name);
            chart.getData().add(series);
        } else {
            series.getData().clear();
        }

        if (!(plottable instanceof XYPlottable)) {
            LoggerFactory.getLogger(getClass()).warn("The provided Plottable is not a subclass of XYPlottable");
        }

        XYAdapter adapter = plottable.adapter();

        Function<DataPoint, Number> xFunc = (DataPoint point) -> adapter.getX(point).numberValue();
        Function<DataPoint, Number> yFunc = (DataPoint point) -> adapter.getY(point).numberValue();

        series.getData().addAll(plottable.dataStream()
                .map(point -> new XYChart.Data<>(xFunc.apply(point), yFunc.apply(point)))
                .collect(Collectors.toList()));

    }

    @Override
    protected void updatePlotConfig(String name) {

    }

    @Override
    protected void updateLegend(Meta legendMeta) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void display(AnchorPane container) {
        AnchorPane.setTopAnchor(chart, 0d);
        AnchorPane.setBottomAnchor(chart, 0d);
        AnchorPane.setRightAnchor(chart, 0d);
        AnchorPane.setLeftAnchor(chart, 0d);
        container.getChildren().add(chart);
    }

    @Override
    public void snapshot(OutputStream stream, Meta config) {
        //FIXME fix method to actually use parameters
        this.chart.snapshot(new Callback<SnapshotResult, Void>() {
            @Override
            public Void call(SnapshotResult result) {
                BufferedImage bImage = SwingFXUtils.fromFXImage(result.getImage(), null);
                try {
                    ImageIO.write(bImage, "png", stream);
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Failed to write image", e);
                }
                return null;
            }
        }, null, null);
    }
}
