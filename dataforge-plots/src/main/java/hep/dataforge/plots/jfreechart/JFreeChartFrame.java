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

import hep.dataforge.data.DataPoint;
import hep.dataforge.data.XYAdapter;
import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.ValueConversionException;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.PlotUtils;
import hep.dataforge.plots.Plottable;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import static java.lang.Double.NaN;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.AnchorPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.SunPNGEncoderAdapter;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
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

    private final JFreeChart chart;
    private final XYPlot plot;

    private static Action exportAction(JFreeChart t) {
        return new AbstractAction("Export JFC") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Export file location");
                JFileChooser fc = new JFileChooser();
                FileFilter filter = new FileNameExtensionFilter("JFC files", "jfc");
                fc.setFileFilter(filter);

                int returnVal = fc.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    ObjectOutputStream stream = null;
                    try {
                        File file = fc.getSelectedFile();

                        String fileName = file.toString();
                        if (!fileName.endsWith(".jfc")) {
                            fileName += ".jfc";
                            file = new File(fileName);
                        }

                        stream = new ObjectOutputStream(new FileOutputStream(file));
                        stream.writeObject(t);
                        frame.dispose();
                    } catch (IOException ex) {
                        Logger.getLogger(JFreeChartFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException ex) {
                                Logger.getLogger(JFreeChartFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                } else {
                    frame.dispose();
                }
            }
        };
    }

    public JFreeChartFrame(String name, Meta meta) {
        super(name);
        if (meta == null) {
            meta = Meta.buildEmpty("plot");
        }
        plot = new XYPlot();

        String title = meta.getString("frameTitle", "");

//        if (title.equals("default")) {
//            title = "";
//        }
        chart = new JFreeChart(title, plot);
        super.configure(meta);
    }

    @Override
    public JFreeChartFrame display(AnchorPane container) {
        Runnable run = () -> {
            SwingNode viewer = new SwingNode();
            JPanel panel = new JPanel(new BorderLayout(), true);

            display(panel);
            viewer.setContent(panel);

            container.getChildren().add(viewer);
            AnchorPane.setBottomAnchor(viewer, 0d);
            AnchorPane.setTopAnchor(viewer, 0d);
            AnchorPane.setLeftAnchor(viewer, 0d);
            AnchorPane.setRightAnchor(viewer, 0d);
        };

        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }

        return this;
    }

    public JFreeChartFrame display(Container panel) {
        ChartPanel cp = new ChartPanel(getChart());
        cp.getPopupMenu().add(new JMenuItem(exportAction(getChart())));

        panel.add(cp);
        panel.revalidate();
        panel.repaint();
        return this;
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
        logAxis.setMinorTickCount(10);
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
        SwingUtilities.invokeLater(() -> {
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
    protected void updateLegend(Meta legendMeta) {
        if (legendMeta.getBoolean("show", true)) {
            if (chart.getLegend() == null) {
                chart.addLegend(new LegendTitle(plot));
            }
        } else {
            chart.removeLegend();
        }
    }

    @Override
    protected synchronized void updateFrame(Meta meta) {
//        plot.getRenderer().setLegendItemLabelGenerator((XYDataset dataset, int series) -> {
//            Plottable p = get(dataset.getSeriesKey(series).toString());
//            return p.getConfig().getString("title", p.getName());
//        });
    }

    protected double convertValue(Value v) {
        try {
            if (v.valueType() == ValueType.NULL) {
                return Double.NaN;
            }
            return v.doubleValue();
        } catch (ValueConversionException ex) {
            return Double.NaN;
        }
    }

    @Override
    protected void updatePlotData(String name) {
        XYPlottable plottable = get(name);

        XYAdapter adapter = plottable.adapter();

        XYIntervalSeries ser = new XYIntervalSeries(plottable.getName());
        for (DataPoint point : plottable.plotData()) {
            double x = convertValue(adapter.getX(point));
            double y = convertValue(adapter.getY(point));
            if (Double.isNaN(x)) {
                LoggerFactory.getLogger(getClass()).warn("Missing x value!");
            } else if (Double.isNaN(y)) {
                ser.add(x, NaN, NaN, NaN, NaN, NaN);
            } else {
                double xErr = convertValue(adapter.getXerr(point));
                double yErr = convertValue(adapter.getYerr(point));
                ser.add(x, x - xErr, x + xErr, y, y - yErr, y + yErr);
            }
        }

        final XYIntervalSeriesCollection data = new XYIntervalSeriesCollection();
        data.addSeries(ser);

        if (!index.contains(plottable.getName())) {
            index.add(plottable.getName());
        }

        SwingUtilities.invokeLater(() -> {
            plot.setDataset(index.indexOf(name), data);
        });
    }

    @Override
    protected void updatePlotConfig(String name) {
        final Plottable plottable = get(name);
        if (!index.contains(plottable.getName())) {
            index.add(plottable.getName());
        }
        int num = index.indexOf(name);

        Meta meta = new Laminate(plottable.meta()).setDescriptor(DescriptorUtils.buildDescriptor(plottable));
        SwingUtilities.invokeLater(() -> {

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

            plot.setRenderer(num, render);

            Color color = PlotUtils.getAWTColor(meta);
            if (color != null) {
                render.setSeriesPaint(0, color);
            } else {
                Paint paint = render.lookupSeriesPaint(0);
                if (paint instanceof Color) {
                    plottable.getConfig().setValue("color", Value.of(PlotUtils.awtColorToString((Color) paint)), false);
                }
            }
            render.setSeriesVisible(0, meta.getBoolean("visible", true));
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
