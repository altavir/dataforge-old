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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.fx.FXObject;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.Plot;
import hep.dataforge.plots.PlotUtils;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.fx.FXPlotFrame;
import hep.dataforge.plots.fx.FXPlotUtils;
import hep.dataforge.values.Value;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;
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
import org.jfree.data.xy.XYDataset;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * @author Alexander Nozik
 */
public class JFreeChartFrame extends XYPlotFrame implements Serializable, FXObject, FXPlotFrame {

    private final JFreeChart chart;
    private final XYPlot plot;

    /**
     * Index mapping names to datasets
     */
    private final Map<String, JFCDataWrapper> index = new HashMap<>();
//    private final Map<String, Integer> index = new HashMap<>();

    private Mode mode = Mode.NONE;

    public JFreeChartFrame() {
        this(Meta.empty());
    }

    public JFreeChartFrame(Meta frameMeta) {
        plot = new XYPlot();
        chart = new JFreeChart(plot);
        configure(frameMeta);
    }

    @Override
    public Node getFXNode() {
        mode = Mode.JAVAFX;
        ChartViewer viewer = new ChartViewer(getChart());

        addExportPlotAction(viewer.getContextMenu(), this);
        return viewer;
    }


    private void addExportPlotAction(ContextMenu menu, JFreeChartFrame frame) {


        Menu parent = menu.getItems().stream()
                .filter(it -> it instanceof javafx.scene.control.Menu && it.getText().equals("Export As"))
                .map(javafx.scene.control.Menu.class::cast)
                .findFirst()
                .orElseGet(() -> {
                    javafx.scene.control.Menu sub = new Menu("Export As");
                    menu.getItems().add(sub);
                    return sub;
                });


        MenuItem dfpExport = FXPlotUtils.getDFPlotExportMenuItem(menu.getOwnerWindow(), frame);

        parent.getItems().add(dfpExport);
    }

    public JFreeChartFrame display(Container panel) {
        mode = Mode.SWING;
        ChartPanel cp = new ChartPanel(getChart());
//        cp.getPopupMenu().add(new JMenuItem(exportAction(getChart())));

        panel.add(cp);
        panel.revalidate();
        panel.repaint();
        return this;
    }

    private void run(Runnable run) {
        if (mode == Mode.NONE || Platform.isFxApplicationThread()) {
            //run immediately
            run.run();
        } else if (mode == Mode.SWING) {
            if (SwingUtilities.isEventDispatchThread()) {
                run.run();
            } else {
                SwingUtilities.invokeLater(run);
            }
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
        if (meta.hasMeta("range")) {
            logAxis.setRange(getRange(meta.getMeta("range")));
        } else {
            logAxis.setAutoRange(meta.getBoolean("autoRange", true));
        }
        logAxis.setAllowNegativesFlag(false);
        logAxis.setAutoRangeNextLogFlag(true);
        logAxis.setStrictValuesFlag(false); // Omit negatives but do not throw exception
        return logAxis;
    }

    @NotNull
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
    protected synchronized void updateAxis(String axisName, Meta axisMeta, Meta plotMeta) {
        run(() -> {
            ValueAxis axis = getAxis(axisMeta);

            String crosshair = axisMeta.getString("crosshair",
                    () -> plotMeta.getString("crosshair"));


            double from = axisMeta.getDouble("range.from", Double.NEGATIVE_INFINITY);

            if (Double.isFinite(from)) {
                axis.setLowerBound(from);
            }

            double to = axisMeta.getDouble("range.to", Double.NEGATIVE_INFINITY);

            if (Double.isFinite(to)) {
                axis.setUpperBound(to);
            }
//            if (Double.isFinite(from) && Double.isFinite(to)) {
//                axis.setRange(from,to);
//            } else {
//                axis.setAutoRange(true);
//            }

            switch (axisName) {
                case "x":
                    plot.setDomainAxis(axis);
                    switch (crosshair) {
                        case "free":
                            plot.setDomainCrosshairVisible(true);
                            plot.setDomainCrosshairLockedOnData(false);
                            break;
                        case "data":
                            plot.setDomainCrosshairVisible(true);
                            plot.setDomainCrosshairLockedOnData(true);
                            break;
                        case "none":
                            plot.setDomainCrosshairVisible(false);
                    }
                    break;
                case "y":
                    plot.setRangeAxis(axis);
                    switch (crosshair) {
                        case "free":
                            plot.setRangeCrosshairVisible(true);
                            plot.setRangeCrosshairLockedOnData(false);
                            break;
                        case "data":
                            plot.setRangeCrosshairVisible(true);
                            plot.setRangeCrosshairLockedOnData(true);
                            break;
                        case "none":
                            plot.setRangeCrosshairVisible(false);
                    }
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
//            Plot p = get(dataset.getSeriesKey(series).toString());
//            return p.getConfig().getString("title", p.getName());
//        });
    }


    @Override
    protected synchronized void updatePlotData(String name, Plot plot) {
        if (!index.containsKey(name)) {
            JFCDataWrapper wrapper = new JFCDataWrapper(plot);
            wrapper.setIndex(index.values().stream().mapToInt(JFCDataWrapper::getIndex).max().orElse(-1) + 1);
            index.put(name, wrapper);
            run(() -> {
                this.plot.setDataset(wrapper.getIndex(), wrapper);
            });
        } else {
            JFCDataWrapper wrapper = index.get(name);
            wrapper.setPlot(plot);
            wrapper.invalidateData();
            run(() -> this.plot.datasetChanged(new DatasetChangeEvent(this.plot, wrapper)));
        }
    }

    @Override
    protected synchronized void updatePlotConfig(String name, Laminate meta) {
        XYLineAndShapeRenderer render;
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
        boolean showLines = meta.getBoolean("showLine", false);
        boolean showSymbols = meta.getBoolean("showSymbol", true);
        render.setDefaultShapesVisible(showSymbols);
        render.setDefaultLinesVisible(showLines);

        //Build Legend map to avoid serialization issues
        double thickness = PlotUtils.getThickness(meta);
        if (thickness > 0) {
            render.setSeriesStroke(0, new BasicStroke((float) thickness));
        }

        Color color = PlotUtils.getAWTColor(meta);
        if (color != null) {
            render.setSeriesPaint(0, color);
        }

        boolean visible = meta
                .collectValue(
                        "visible",
                        Collectors.reducing(Value.of(true), (v1, v2) -> Value.of(v1.booleanValue() && v2.booleanValue()))
                )
                .booleanValue();

        render.setSeriesVisible(0, visible);

        run(() -> {
            plot.setRenderer(index.get(name).getIndex(), render);

            // update configuration to default colors
            if (color == null) {
                Paint paint = render.lookupSeriesPaint(0);
                if (paint instanceof Color) {
                    //TODO replace by meta overlay
                    getPlots().opt(name)
                            .ifPresent(it -> it.configureValue("color", Value.of(PlotUtils.awtColorToString((Color) paint))));
                }
            }
        });
    }

    /**
     * Take a snapshot of plot frame and save it in a given OutputStream
     *
     * @param stream
     * @param cfg
     */
    @Override
    public synchronized void save(OutputStream stream, Meta cfg) {
        new Thread(() -> {
            try {
                new SunPNGEncoderAdapter().encode(chart.createBufferedImage(cfg.getInt("width", 800), cfg.getInt("height", 600)), stream);
            } catch (IOException ex) {
                LoggerFactory.getLogger(getClass()).error("IO error during image encoding", ex);
            }
        }).start();
    }

    private enum Mode {
        SWING, // Swing UI thread mode
        JAVAFX, // JavaFX UI thread mode
        NONE // current thread mode
    }

    @Override
    public synchronized void remove(String plotName) {
        super.remove(plotName);
        run(() -> {
            plot.setDataset(index.get(plotName).getIndex(), null);
        });
        index.remove(plotName);
    }

    @Override
    public synchronized void clear() {
        super.clear();
        this.index.clear();
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
}
