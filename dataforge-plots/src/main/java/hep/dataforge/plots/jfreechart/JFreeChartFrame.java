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
package hep.dataforge.plots.jfreechart;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.ValueConversionException;
import hep.dataforge.fx.FXUtils;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.PlotUtils;
import hep.dataforge.plots.Plottable;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.plots.fx.FXPlotUtils;
import hep.dataforge.values.Value;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Paint;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.SunPNGEncoderAdapter;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class JFreeChartFrame extends XYPlotFrame implements Serializable {

    /**
     * Index mapping names to datasets
     */
    List<String> index = new ArrayList<>();

    private boolean swingMode = false;

    private final JFreeChart chart;
    private final XYPlot plot;

    public JFreeChartFrame() {
        plot = new XYPlot();
        chart = new JFreeChart(plot);
    }

    public JFreeChartFrame(Meta frameMeta) {
        this();
        super.configure(frameMeta);
    }

    @Override
    public JFreeChartFrame display(AnchorPane container) {
        Runnable run = () -> {
            ChartViewer viewer = new ChartViewer(getChart());

            FXPlotUtils.addExportPlotAction(viewer.getContextMenu(), this);

            container.getChildren().add(viewer);
            AnchorPane.setBottomAnchor(viewer, 0d);
            AnchorPane.setTopAnchor(viewer, 0d);
            AnchorPane.setLeftAnchor(viewer, 0d);
            AnchorPane.setRightAnchor(viewer, 0d);
        };

        FXUtils.run(run);

        return this;
    }

    public JFreeChartFrame display(Container panel) {
        ChartPanel cp = new ChartPanel(getChart());
//        cp.getPopupMenu().add(new JMenuItem(exportAction(getChart())));

        panel.add(cp);
        panel.revalidate();
        panel.repaint();
        swingMode = true;
        return this;
    }

    private void run(Runnable run) {
        if (swingMode) {
            if (SwingUtilities.isEventDispatchThread()) {
                run.run();
            } else {
                SwingUtilities.invokeLater(run);
            }
        } else if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
    }

    public JFreeChart getChart() {
        return chart;
    }

    private ValueAxis getNumberAxis(Meta annotation) {
        NumberAxis axis = new NumberAxis();
        axis.setAutoRangeIncludesZero(false);
        axis.setAutoRangeStickyZero(false);
        return axis;
    }

    private DateAxis getDateAxis(Meta annotation) {
        DateAxis axis = new DateAxis();
        axis.setTimeZone(TimeZone.getTimeZone("UTC"));
        return axis;
    }

    private ValueAxis getLogAxis(Meta meta) {
        //FIXME autorange with negative values
        LogarithmicAxis logAxis = new LogarithmicAxis("");
//        logAxis.setMinorTickCount(10);
        logAxis.setExpTickLabelsFlag(true);
        logAxis.setMinorTickMarksVisible(true);
        if (meta.hasNode("range")) {
            logAxis.setRange(getRange(meta.getNode("range")));
        } else {
            logAxis.setAutoRange(meta.getBoolean("autoRange", true));
        }
        logAxis.setAllowNegativesFlag(false);
        logAxis.setAutoRangeNextLogFlag(true);
        logAxis.setStrictValuesFlag(false); // Ommit negatives but do not throw exception
        return logAxis;
    }

    private Range getRange(Meta meta) {
        return new Range(meta.getDouble("lower", Double.NEGATIVE_INFINITY), meta.getDouble("upper", Double.POSITIVE_INFINITY));
    }

    private ValueAxis getAxis(Meta axisMeta) {
        switch (axisMeta.getString("type", "number")) {
            case "log":
                return getLogAxis(axisMeta);
            case "time":
                return getDateAxis(axisMeta);
            default:
                return getNumberAxis(axisMeta);
        }
    }

    @Override
    protected synchronized void updateAxis(String axisName, Meta axisMeta) {
        run(() -> {
            ValueAxis axis = getAxis(axisMeta);

            switch (axisName) {
                case "x":
                    plot.setDomainAxis(axis);
                    break;
                case "y":
                    plot.setRangeAxis(axis);
                    break;
                default:
                    throw new NameNotFoundException(axisName, "No such axis in this plot");
            }

            if (axisMeta.hasValue("axisTitle")) {
                String label = axisMeta.getString("axisTitle");
                if (axisMeta.hasValue("axisUnits")) {
                    label += " (" + axisMeta.getString("axisUnits") + ")";
                }
                axis.setLabel(label);
            }
        });
    }

    @Override
    protected synchronized void updateLegend(Meta legendMeta) {
        run(() -> {
            if (legendMeta.getBoolean("show", true)) {
                if (chart.getLegend() == null) {
                    chart.addLegend(new LegendTitle(plot));
                }
            } else {
                chart.removeLegend();
            }
        });
    }

    @Override
    protected synchronized void updateFrame(Meta meta) {
        run(() -> {
            this.chart.setTitle(meta.getString("title", ""));
        });
//        plot.getRenderer().setLegendItemLabelGenerator((XYDataset dataset, int series) -> {
//            Plottable p = get(dataset.getSeriesKey(series).toString());
//            return p.getConfig().getString("title", p.getName());
//        });
    }

    protected double convertValue(Value v) {
        try {
            switch (v.valueType()) {
                case NULL:
                    return Double.NaN;
//                case TIME:
//                    return v.timeValue().toEpochMilli();
                default:
                    return v.doubleValue();
            }
        } catch (ValueConversionException ex) {
            return Double.NaN;
        }
    }

    @Override
    protected synchronized void updatePlotData(String name) {
        //removing data set if necessary
        XYPlottable plottable = get(name);
        if (plottable == null) {
            index.remove(name);
            run(() -> {
                plot.setDataset(index.indexOf(name), null);
            });
            return;
        }

        if (!index.contains(name)) {
            IntervalXYDataset data = new JFCDataWrapper(plottable);
            index.add(plottable.getName());

            run(() -> {
                plot.setDataset(index.indexOf(name), data);
            });
        } else {
            JFCDataWrapper wrapper = (JFCDataWrapper) plot.getDataset(index.indexOf(name));
            wrapper.clearCache();
            run(() -> plot.datasetChanged(new DatasetChangeEvent(plot, wrapper)));
        }
    }

    @Override
    protected synchronized void updatePlotConfig(String name) {
        final Plottable plottable = get(name);
        if (!index.contains(plottable.getName())) {
            index.add(plottable.getName());
        }
        int num = index.indexOf(name);

        Meta meta = new Laminate(plottable.meta()).setDescriptor(DescriptorUtils.buildDescriptor(plottable));
        run(() -> {

            XYLineAndShapeRenderer render;
            boolean showLines = meta.getBoolean("showLine", false);
            boolean showSymbols = meta.getBoolean("showSymbol", true);
            if (meta.getBoolean("showErrors", false)) {
                render = new XYErrorRenderer();
            } else {
                switch (meta.getString("connectionType", "default")) {
                    case "step":
                        render = new XYStepRenderer();
                        break;
                    case "spline":
                        render = new XYSplineRenderer();
                        break;
                    default:
                        render = new XYLineAndShapeRenderer();
                }
            }
            render.setBaseShapesVisible(showSymbols);
            render.setBaseLinesVisible(showLines);

            //Build Legend map to avoid serialization issues
//            Map<String, String> titleMap = new HashMap<>();
//            plottables.entrySet().stream().forEach((entry) -> {
//                titleMap.put(entry.getKey(), entry.getValue().getConfig().getString("title", entry.getKey()));
//            });
//            
//            
//            render.setLegendItemLabelGenerator(new LabelGenerator(titleMap));
            double thickness = PlotUtils.getThickness(meta);
            if (thickness > 0) {
                render.setSeriesStroke(0, new BasicStroke((float) thickness));
            }

            Color color = PlotUtils.getAWTColor(meta);
            if (color != null) {
                render.setSeriesPaint(0, color);
            }

            render.setSeriesVisible(0, meta.getBoolean("visible", true));
            plot.setRenderer(num, render);

            // update configuration to default colors
            if (color == null) {
                Paint paint = render.lookupSeriesPaint(0);
                if (paint instanceof Color) {
                    plottable.getConfig().setValue("color", Value.of(PlotUtils.awtColorToString((Color) paint)), false);
                }
            }
        });
    }

    private static class LabelGenerator implements XYSeriesLabelGenerator, Serializable {

        private final Map<String, String> titleMap;

        public LabelGenerator(Map<String, String> titleMap) {
            this.titleMap = titleMap;
        }

        @Override
        public String generateLabel(XYDataset dataset, int series) {
            return titleMap.get(dataset.getSeriesKey(series).toString());
        }

    }

    /**
     * Take a snapshot of plot frame and save it in a given OutputStream
     *
     * @param stream
     * @param cfg
     */
    @ValueDef(name = "width", type = "NUMBER", def = "800", info = "The width of the snapshot in pixels")
    @ValueDef(name = "height", type = "NUMBER", def = "600", info = "The height of the snapshot in pixels")
    public synchronized void toPNG(OutputStream stream, Meta cfg) {
        SwingUtilities.invokeLater(() -> {
            try {
                new SunPNGEncoderAdapter().encode(chart.createBufferedImage(cfg.getInt("width", 800), cfg.getInt("height", 600)), stream);
            } catch (IOException ex) {
                LoggerFactory.getLogger(getClass()).error("IO error during image encoding", ex);
            }
        });
    }
}
