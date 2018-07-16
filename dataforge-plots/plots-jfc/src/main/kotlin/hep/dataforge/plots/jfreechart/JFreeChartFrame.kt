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
package hep.dataforge.plots.jfreechart

import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.plots.Plot
import hep.dataforge.plots.PlotFrame
import hep.dataforge.plots.PlotUtils
import hep.dataforge.plots.XYPlotFrame
import hep.dataforge.useValue
import hep.dataforge.utils.FXObject
import hep.dataforge.values.Value
import hep.dataforge.values.ValueFactory
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.LogarithmicAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.encoders.SunPNGEncoderAdapter
import org.jfree.chart.fx.ChartViewer
import org.jfree.chart.labels.XYSeriesLabelGenerator
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYErrorRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.renderer.xy.XYSplineRenderer
import org.jfree.chart.renderer.xy.XYStepRenderer
import org.jfree.chart.title.LegendTitle
import org.jfree.data.Range
import org.jfree.data.general.DatasetChangeEvent
import org.jfree.data.xy.XYDataset
import org.slf4j.LoggerFactory
import tornadofx.*
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Shape
import java.io.IOException
import java.io.ObjectStreamException
import java.io.OutputStream
import java.io.Serializable
import java.util.*
import java.util.stream.Collectors

/**
 * @author Alexander Nozik
 */
class JFreeChartFrame @JvmOverloads constructor(frameMeta: Meta = Meta.empty()) : XYPlotFrame(), FXObject, Serializable {

    private val xyPlot: XYPlot = XYPlot()
    val chart: JFreeChart = JFreeChart(xyPlot)

    /**
     * Index mapping names to datasets
     */
    @Transient
    private val index = HashMap<Name, JFCDataWrapper>()
    /**
     * Caches for color and shape
     */
    @Transient
    private val colorCache = HashMap<Name, Color>()
    @Transient
    private val shapeCache = HashMap<Name, Shape>()

    init {
        configure(frameMeta)
    }

    private fun runLater(runnable: () -> Unit) {
        if(FX.initialized.value){
            Platform.runLater(runnable)
        } else{
            runnable.invoke()
        }
    }

    override fun getFXNode(): Node {
        val viewer = ChartViewer(chart)
        addExportPlotAction(viewer.contextMenu, this)
        return viewer
    }


    private fun addExportPlotAction(menu: ContextMenu, frame: JFreeChartFrame) {
        val parent = menu.items.stream()
                .filter { it -> it is Menu && it.getText() == "Export As" }
                .map<Menu> { Menu::class.java.cast(it) }
                .findFirst()
                .orElseGet {
                    val sub = Menu("Export As")
                    menu.items.add(sub)
                    sub
                }


        val dfpExport = PlotUtils.getDFPlotExportMenuItem(menu.ownerWindow, frame)

        parent.items.add(dfpExport)
    }


    private fun getNumberAxis(meta: Meta): ValueAxis {
        val axis = NumberAxis()
        axis.autoRangeIncludesZero = meta.getBoolean("includeZero", false)
        axis.autoRangeStickyZero = meta.getBoolean("stickyZero", false)
        return axis
    }

    private fun getDateAxis(meta: Meta): DateAxis {
        val axis = DateAxis()
        axis.timeZone = TimeZone.getTimeZone(meta.getString("timeZone", "UTC"))
        return axis
    }

    private fun getLogAxis(meta: Meta): ValueAxis {
        //FIXME autorange with negative values
        val logAxis = LogarithmicAxis("")
        //        logAxis.setMinorTickCount(10);
        logAxis.expTickLabelsFlag = true
        logAxis.isMinorTickMarksVisible = true
        if (meta.hasMeta("range")) {
            logAxis.range = getRange(meta.getMeta("range"))
        } else {
            logAxis.isAutoRange = meta.getBoolean("autoRange", true)
        }
        logAxis.allowNegativesFlag = false
        logAxis.autoRangeNextLogFlag = true
        logAxis.strictValuesFlag = false // Omit negatives but do not throw exception
        return logAxis
    }

    private fun getRange(meta: Meta): Range {
        return Range(meta.getDouble("lower", java.lang.Double.NEGATIVE_INFINITY), meta.getDouble("upper", java.lang.Double.POSITIVE_INFINITY))
    }

    private fun getAxis(axisMeta: Meta): ValueAxis {
        return when (axisMeta.getString("type", "number")) {
            "log" -> getLogAxis(axisMeta)
            "time" -> getDateAxis(axisMeta)
            else -> getNumberAxis(axisMeta)
        }
    }

    @Synchronized
    override fun updateAxis(axisName: String, axisMeta: Meta, plotMeta: Meta) {
        runLater {
            val axis = getAxis(axisMeta)

            val crosshair = axisMeta.getString("crosshair") { plotMeta.getString("crosshair", "none") }


            val from = axisMeta.getDouble("range.from", java.lang.Double.NEGATIVE_INFINITY)

            if (java.lang.Double.isFinite(from)) {
                axis.lowerBound = from
            }

            val to = axisMeta.getDouble("range.to", java.lang.Double.NEGATIVE_INFINITY)

            if (java.lang.Double.isFinite(to)) {
                axis.upperBound = to
            }
            //            if (Double.isFinite(from) && Double.isFinite(to)) {
            //                axis.setRange(from,to);
            //            } else {
            //                axis.setAutoRange(true);
            //            }

            when (axisName) {
                "x" -> {
                    xyPlot.domainAxis = axis
                    when (crosshair) {
                        "free" -> {
                            xyPlot.isDomainCrosshairVisible = true
                            xyPlot.isDomainCrosshairLockedOnData = false
                        }
                        "data" -> {
                            xyPlot.isDomainCrosshairVisible = true
                            xyPlot.isDomainCrosshairLockedOnData = true
                        }
                        "none" -> xyPlot.isDomainCrosshairVisible = false
                    }
                }
                "y" -> {
                    xyPlot.rangeAxis = axis
                    when (crosshair) {
                        "free" -> {
                            xyPlot.isRangeCrosshairVisible = true
                            xyPlot.isRangeCrosshairLockedOnData = false
                        }
                        "data" -> {
                            xyPlot.isRangeCrosshairVisible = true
                            xyPlot.isRangeCrosshairLockedOnData = true
                        }
                        "none" -> xyPlot.isRangeCrosshairVisible = false
                    }
                }
                else -> throw NameNotFoundException(axisName, "No such axis in this plot")
            }

            if (axisMeta.hasValue("title")) {
                var label = axisMeta.getString("title")
                if (axisMeta.hasValue("units")) {
                    label += " (" + axisMeta.getString("units") + ")"
                }
                axis.label = label
            }

        }
    }

    @Synchronized
    override fun updateLegend(legendMeta: Meta) {
        runLater {
            if (legendMeta.getBoolean("show", true)) {
                if (chart.legend == null) {
                    chart.addLegend(LegendTitle(xyPlot))
                }
            } else {
                chart.removeLegend()
            }
            this.xyPlot.legendItems
        }
    }

    @Synchronized
    override fun updateFrame(annotation: Meta) {
        runLater { this.chart.setTitle(annotation.getString("title", "")) }
    }

    override fun plotRemoved(name: Name) {
        val num = Optional.ofNullable(index[name]).map<Int> { it.index }.orElse(-1)
        if (num >= 0) {
            runLater { xyPlot.setDataset(num, null) }
        }
        index.remove(name)
    }

    @Synchronized
    override fun updatePlotData(name: Name, plot: Plot) {
        if (!index.containsKey(name)) {
            val wrapper = JFCDataWrapper(plot)
            wrapper.index = index.values.stream().mapToInt { it.index }.max().orElse(-1) + 1
            index[name] = wrapper
            runLater { this.xyPlot.setDataset(wrapper.index, wrapper) }
        } else {
            val wrapper = index[name]
            wrapper!!.setPlot(plot)
            wrapper.invalidateData()
            runLater { this.xyPlot.datasetChanged(DatasetChangeEvent(this.xyPlot, wrapper)) }
        }
    }

    @Synchronized
    override fun updatePlotConfig(name: Name, config: Laminate) {
        val render: XYLineAndShapeRenderer = if (config.getBoolean("showErrors", true)) {
            XYErrorRenderer()
        } else {
            when (config.getString("connectionType", "DEFAULT").toUpperCase()) {
                "STEP" -> XYStepRenderer()
                "SPLINE" -> XYSplineRenderer()
                else -> XYLineAndShapeRenderer()
            }
        }
        val showLines = config.getBoolean("showLine", false)
        val showSymbols = config.getBoolean("showSymbol", true)
        render.defaultShapesVisible = showSymbols
        render.defaultLinesVisible = showLines

        //Build Legend map to avoid serialization issues
        val thickness = PlotUtils.getThickness(config)
        if (thickness > 0) {
            render.setSeriesStroke(0, BasicStroke(thickness.toFloat()))
        }

        val color = PlotUtils.getAWTColor(config, colorCache[name])
        if (color != null) {
            render.setSeriesPaint(0, color)
        }

        val shape = shapeCache[name]
        if (shape != null) {
            render.setSeriesShape(0, shape)
        }

        val visible = config
                .collectValue(
                        "visible",
                        Collectors.reducing(ValueFactory.of(true)) { v1: Value, v2: Value -> ValueFactory.of(v1.boolean && v2.boolean) }
                )
                .boolean

        render.setSeriesVisible(0, visible)
        config.useValue("title") {
            render.setLegendItemLabelGenerator { dataset, series -> it.string }
        }

        runLater {
            xyPlot.setRenderer(index[name]!!.index, render)

            // update cache to default colors
            val paint = render.lookupSeriesPaint(0)
            if (paint is Color) {
                colorCache[name] = paint
            }
            shapeCache[name] = render.lookupSeriesShape(0)
        }
    }

    /**
     * Take a snapshot of plot frame and save it in a given OutputStream
     *
     * @param stream
     * @param config
     */
    @Synchronized
    override fun asImage(stream: OutputStream, config: Meta) {
        Thread {
            try {
                SunPNGEncoderAdapter().encode(chart.createBufferedImage(config.getInt("width", 800), config.getInt("height", 600)), stream)
            } catch (ex: IOException) {
                LoggerFactory.getLogger(javaClass).error("IO error during image encoding", ex)
            }
        }.start()
    }

    override fun getActualColor(name: Name): Optional<Value> {
        return Optional.ofNullable(colorCache[name]).map { color -> ValueFactory.of(PlotUtils.awtColorToString(color)) }
    }

    //    @Override
    //    public synchronized void clear() {
    //        super.clear();
    //        index.values().forEach(wr -> {
    //            xyPlot.setDataset(wr.getIndex(), null);
    //        });
    //        this.index.clear();
    //    }

    @Throws(ObjectStreamException::class)
    private fun writeReplace(): Any {
        return PlotFrame.PlotFrameEnvelope(PlotFrame.wrapper.wrap(this))
    }

    private class LabelGenerator(private val titleMap: Map<String, String>) : XYSeriesLabelGenerator, Serializable {

        override fun generateLabel(dataset: XYDataset, series: Int): String {
            return titleMap[dataset.getSeriesKey(series).toString()] ?: dataset.getSeriesKey(series).toString()
        }

    }
}
