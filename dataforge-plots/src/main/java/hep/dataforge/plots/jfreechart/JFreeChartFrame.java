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
import hep.dataforge.data.XYDataAdapter;
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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.AnchorPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
     * Десериализует график из файла и прицепляет его к новой формочке
     *
     * @param stream
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static JFrame deserialize(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        JFreeChart jfc = (JFreeChart) stream.readObject();

        JFrame frame = new JFrame("DataForge visualisator");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(800, 600));
            frame.setContentPane(panel);

            panel.removeAll();
            panel.add(new ChartPanel(jfc));
            panel.revalidate();
            panel.repaint();

            frame.pack();
            frame.setVisible(true);
        });
        return frame;
    }

    /**
     * Index mapping names to datasets
     */
    List<String> index = new ArrayList<>();

    private JFreeChart chart;
    private XYPlot plot;

    /**
     * Draw new JFrame containing this plot
     *
     * @param name
     * @param annotation
     * @return
     */
    public static JFreeChartFrame drawFrame(String name, Meta annotation) {
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(800, 600));
        frame.setContentPane(panel);

        SwingUtilities.invokeLater(() -> {
            frame.pack();
            frame.setVisible(true);
        });
        return new JFreeChartFrame(name, annotation, panel);
    }

    public JFreeChartFrame(String name, Meta annotation, Container panel) {
        this(name, annotation, (JFreeChart t) -> {
            panel.removeAll();
            ChartPanel cp = new ChartPanel(t);

            cp.getPopupMenu().add(new JMenuItem(exportAction(t)));

            panel.add(cp);
            panel.revalidate();
            panel.repaint();
        });
    }

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

    /**
     * Отрисовка компонента при помощи SwingNode
     *
     * @param name
     * @param annotation
     * @param parent
     */
    public JFreeChartFrame(String name, Meta annotation, AnchorPane parent) {
        this(name, annotation, (JFreeChart t) -> {
//            ChartViewer viewer = new ChartViewer(t, true);
            SwingNode viewer = new SwingNode();
            JPanel panel = new JPanel(new BorderLayout(), true);
            panel.removeAll();

            ChartPanel cp = new ChartPanel(t);
            cp.getPopupMenu().add(new JMenuItem(exportAction(t)));
            panel.add(cp);
            panel.revalidate();
            panel.repaint();
            viewer.setContent(panel);

            parent.getChildren().add(viewer);
            AnchorPane.setBottomAnchor(viewer, 0d);
            AnchorPane.setTopAnchor(viewer, 0d);
            AnchorPane.setLeftAnchor(viewer, 0d);
            AnchorPane.setRightAnchor(viewer, 0d);

            parent.widthProperty().addListener(
                    (ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
                        panel.repaint();
                    });

            parent.heightProperty().addListener(
                    (ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) -> {
                        panel.repaint();
                    });
        });
    }

    private JFreeChartFrame(String name, Meta meta, Consumer<JFreeChart> container) {
        super(name, meta);
        if (meta == null) {
            meta = Meta.buildEmpty("plot");
        }
        ValueAxis xaxis = getNumberAxis(meta.getNode("xAxis", Meta.buildEmpty("xAxis")));
        ValueAxis yaxis = getNumberAxis(meta.getNode("yAxis", Meta.buildEmpty("YAxis")));
        plot = new XYPlot(new XYIntervalSeriesCollection(), xaxis, yaxis, new XYErrorRenderer());

        String title = meta.getString("frameTitle", name);

        if (title.equals("default")) {
            title = "";
        }

        chart = new JFreeChart(title, plot);
        applyConfig(meta);
        container.accept(chart);
    }

    protected void attachTo(Consumer<JFreeChart> consumer) {
        consumer.accept(chart);
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

    private ValueAxis getLogAxis(Meta annotation) {
        LogarithmicAxis logAxis = new LogarithmicAxis("");
        logAxis.setMinorTickCount(10);
        logAxis.setExpTickLabelsFlag(true);
        logAxis.setMinorTickMarksVisible(true);
        logAxis.setAutoRange(true);
        logAxis.setAllowNegativesFlag(false);
//        logAxis.setAutoTickUnitSelection(false);
//        logAxis.setNumberFormatOverride(new DecimalFormat("0E0"));
//        logAxis.setSmallestValue(1e-20);
        return logAxis;
    }

    @Override
    protected synchronized void updateAxis(String axisName, Meta annotation) {
        SwingUtilities.invokeLater(() -> {
            ValueAxis axis;
            switch (axisName) {
                case "x":
                    if (annotation.getBoolean("logAxis", false)) {
                        plot.setDomainAxis(getLogAxis(annotation));
                    } else if (annotation.getBoolean("timeAxis", false)) {
                        plot.setDomainAxis(getDateAxis(annotation));
                    }
                    axis = plot.getDomainAxis();
                    break;
                case "y":

                    if (annotation.getBoolean("logAxis", false)) {
                        plot.setRangeAxis(getLogAxis(annotation));
                    }
                    axis = plot.getRangeAxis();
                    break;
                default:
                    throw new NameNotFoundException(axisName, "No such axis in this plot");
            }

            if (annotation.hasValue("axisTitle")) {
                String label = annotation.getString("axisTitle");
                if (annotation.hasValue("axisUnits")) {
                    label += " (" + annotation.getString("axisUnits") + ")";
                }
                axis.setLabel(label);
            }
        });
    }

    @Override
    protected synchronized void updateFrame(Meta annotation) {
//        plot.getRenderer().setLegendItemLabelGenerator((XYDataset dataset, int series) -> {
//            Plottable p = get(dataset.getSeriesKey(series).toString());
//            return p.getConfig().getString("title", p.getName());
//        });
    }

    protected Double convertValue(Value v) {
        try {
            Double res = v.doubleValue();
            if (Double.isNaN(res)) {
                return null;
            } else {
                return v.doubleValue();
            }
        } catch (ValueConversionException ex) {
            return null;
        }
    }

    @Override
    protected void updatePlotData(String name) {
        XYPlottable plottable = get(name);

        XYIntervalSeries ser = new XYIntervalSeries(plottable.getName());
        XYDataAdapter adapter = plottable.adapter();
        for (DataPoint point : plottable.plotData()) {
            Double x = convertValue(adapter.getX(point));
            if (x != null) {
                double y = convertValue(adapter.getY(point));
                double xErr = convertValue(adapter.getXerr(point));
                double yErr = convertValue(adapter.getYerr(point));
                ser.add(x, x - xErr, x + xErr, y, y - yErr, y + yErr);
            } else {
                ser.add(null, true);
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

        Meta meta = new Laminate(plottable.getConfig(), meta()).setDescriptor(DescriptorUtils.buildDescriptor(plottable));
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

            Color color = PlotUtils.getColor(meta);
            if (color != null) {
                render.setSeriesPaint(0, color);
            }

            render.setSeriesVisible(0, meta.getBoolean("visible", true));

            plot.setRenderer(num, render);
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
    @ValueDef(name = "width", def = "800", info = "The width of the snapshot in pixels")
    @ValueDef(name = "height", def = "600", info = "The height of the snapshot in pixels")
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
