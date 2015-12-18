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
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.XYDataAdapter;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.XYPlottable;
import java.util.function.Function;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class FXLineChartFrame extends XYPlotFrame {

    LineChart<Number, Number> chart;

    public FXLineChartFrame(String name, Meta annotation, LineChart<Number, Number> chart) {
        super(name, annotation);
        this.chart = chart;
    }

    /**
     * Вставить и растянуть на всю ширину
     *
     * @param name
     * @param pane
     * @param annotation
     */
    public FXLineChartFrame(String name, Meta annotation, AnchorPane pane) {
        super(name, annotation);
        this.chart = new LineChart<>(new NumberAxis(), new NumberAxis());
        AnchorPane.setTopAnchor(chart, 0d);
        AnchorPane.setBottomAnchor(chart, 0d);
        AnchorPane.setRightAnchor(chart, 0d);
        AnchorPane.setLeftAnchor(chart, 0d);
        pane.getChildren().add(chart);
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

        XYDataAdapter adapter = plottable.adapter();
        
        Function<DataPoint, Number> xFunc = (DataPoint point) -> adapter.getX(point).numberValue();
        Function<DataPoint, Number> yFunc = (DataPoint point) -> adapter.getY(point).numberValue();

        for (DataPoint point : plottable.plotData()) {
            Number x = xFunc.apply(point);
            Number y = yFunc.apply(point);
            series.getData().add(new XYChart.Data<>(x, y));
        }
    }

    @Override
    protected void updatePlotConfig(String name) {

    }

}
