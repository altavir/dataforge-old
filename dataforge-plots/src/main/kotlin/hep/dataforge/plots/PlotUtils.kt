/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.plots

import hep.dataforge.context.Context
import hep.dataforge.io.envelopes.DefaultEnvelopeType
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter
import hep.dataforge.io.envelopes.xmlMetaType
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.plots.data.DataPlot
import hep.dataforge.tables.Adapters
import hep.dataforge.tables.ListTable
import hep.dataforge.tables.Table
import javafx.scene.control.MenuItem
import javafx.stage.FileChooser
import javafx.stage.Window
import java.awt.Color
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author darksnake
 */
object PlotUtils {

    fun getAWTColor(meta: Meta, def: Color): Color {
        if (meta.hasValue("color")) {
            val fxColor = javafx.scene.paint.Color.valueOf(meta.getString("color"))
            return Color(fxColor.red.toFloat(), fxColor.green.toFloat(), fxColor.blue.toFloat())
        } else {
            return def
        }
    }

    fun awtColorToString(color: Color): String {
        val fxColor = javafx.scene.paint.Color.rgb(
                color.red,
                color.green,
                color.blue,
                color.transparency.toDouble()
        )
        return String.format("#%02X%02X%02X",
                (fxColor.red * 255).toInt(),
                (fxColor.green * 255).toInt(),
                (fxColor.blue * 255).toInt())
    }

    fun getThickness(reader: Meta): Double {
        return reader.getDouble("thickness", -1.0)
    }

    /**
     * Строка для отображениея в легенде
     *
     * @return a [java.lang.String] object.
     */
    fun getTitle(reader: Meta): String {
        return reader.getString("title", "")
    }

    //TODO change arguments order, introduce defaults
    fun setXAxis(frame: PlotFrame, title: String, units: String, type: String) {
        val builder = MetaBuilder("xAxis")
                .setValue("title", title)
                .setValue("units", units)
                .setValue("type", type)
        frame.config.setNode(builder)
    }

    fun setYAxis(frame: PlotFrame, title: String, units: String, type: String) {
        val builder = MetaBuilder("yAxis")
                .setValue("title", title)
                .setValue("units", units)
                .setValue("type", type)
        frame.config.setNode(builder)
    }

    fun setTitle(frame: PlotFrame, title: String) {
        frame.configureValue("title", title)
    }

    fun getPlotManager(context: Context): PlotPlugin {
        return context[PlotPlugin::class.java]
    }

    /**
     * TODO move from plots module to implementations
     *
     * @param window
     * @param frame
     * @return
     */
    fun getDFPlotExportMenuItem(window: Window, frame: PlotFrame): MenuItem {
        val dfpExport = MenuItem("DF...")
        dfpExport.setOnAction { event ->
            val chooser = FileChooser()
            chooser.extensionFilters.setAll(FileChooser.ExtensionFilter("DataForge envelope", "*.df"))
            chooser.title = "Select file to save plot into"
            val file = chooser.showSaveDialog(window)
            if (file != null) {
                try {
                    DefaultEnvelopeWriter(DefaultEnvelopeType.INSTANCE, xmlMetaType)
                            .write(FileOutputStream(file), PlotFrame.Wrapper().wrap(frame))
                } catch (ex: IOException) {
                    throw RuntimeException("Failed to save plot to file", ex)
                }

            }
        }
        return dfpExport
    }

    fun extractData(plot: DataPlot, query: Meta): Table {
        return ListTable(Adapters.getFormat(plot.adapter, Adapters.X_VALUE_KEY, Adapters.Y_VALUE_KEY), plot.getData(query))

    }
}
