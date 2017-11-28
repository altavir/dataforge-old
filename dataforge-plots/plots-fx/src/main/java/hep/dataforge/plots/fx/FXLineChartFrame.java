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

import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.plots.Plot;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.data.XYPlot;
import hep.dataforge.tables.Adapters;
import hep.dataforge.utils.FXObject;
import hep.dataforge.values.Values;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotResult;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
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
public class FXLineChartFrame extends XYPlotFrame implements FXObject {

    LineChart<Number, Number> chart;

    /**
     * Вставить и растянуть на всю ширину
     */
    public FXLineChartFrame() {
        this.chart = new LineChart<>(new NumberAxis(), new NumberAxis());
    }

    XYChart.Series<Number, Number> getSeries(Name name) {
        XYChart.Series<Number, Number> series = null;
        for (XYChart.Series ser : chart.getData()) {
            if (ser != null && name.toString().equals(ser.getName())) {
                series = ser;
                break;
            }
        }
        return series;
    }

    @Override
    protected void updateAxis(String axisName, Meta axisMeta, Meta plotMeta) {

    }

    @Override
    protected void updateFrame(Meta annotation) {

    }

    @Override
    protected void updatePlotData(Name name, @NotNull Plot plot) {
        XYChart.Series<Number, Number> series = getSeries(name);

        if (series == null) {
            series = new XYChart.Series<>();
            series.setName(name.toString());
            chart.getData().add(series);
        } else {
            series.getData().clear();
        }

        if (!(plot instanceof XYPlot)) {
            LoggerFactory.getLogger(getClass()).warn("The provided Plot is not a subclass of XYPlot");
        }

        Function<Values, Number> xFunc = (Values point) -> Adapters.getXValue(plot.getAdapter(),point).numberValue();
        Function<Values, Number> yFunc = (Values point) ->  Adapters.getYValue(plot.getAdapter(),point).numberValue();

        //TODO apply filtering here

        series.getData().addAll(plot.getData().stream()
                .map(point -> new XYChart.Data<>(xFunc.apply(point), yFunc.apply(point)))
                .collect(Collectors.toList()));

    }

    @Override
    protected void updatePlotConfig(Name name, Laminate laminate) {

    }

    @Override
    protected void updateLegend(Meta legendMeta) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node getFXNode() {
        return chart;
    }

    @Override
    public void asImage(OutputStream stream, Meta config) {
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

    @Override
    public void plotRemoved(Name name) {

    }
}
