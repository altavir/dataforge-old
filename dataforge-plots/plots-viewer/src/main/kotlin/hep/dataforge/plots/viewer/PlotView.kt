/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.viewer

import hep.dataforge.io.envelopes.EnvelopeType
import hep.dataforge.plots.fx.FXPlotFrame
import hep.dataforge.plots.fx.PlotContainer
import hep.dataforge.plots.wrapper.PlotUnWrapper
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Controller for ViewerApp

 * @author Alexander Nozik
 */
class PlotView : View("DataForge plot viewer") {
    override val root: Parent by fxml("/fxml/PlotViewer.fxml");
    private val loadButton: Button by fxid();
    private val tabs: TabPane by fxid();

    private val plotMap = HashMap<File, PlotContainer>()

    init{
        loadButton.setOnAction {
            val chooser = FileChooser()
            chooser.title = "Select plot file to load"
            chooser.extensionFilters.setAll(FileChooser.ExtensionFilter("DataForge plot", "*.dfp"))
            val list = chooser.showOpenMultipleDialog(loadButton.scene.window)
            list.forEach { f ->
                try {
                    loadPlot(f)
                } catch (ex: IOException) {
                    LoggerFactory.getLogger(javaClass).error("Failed to load dfp file", ex)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun loadPlot(file: File) {
        val container: PlotContainer = plotMap.getOrElse(file){
            val pane = AnchorPane()
            val tab = Tab(file.name, pane)
            tab.setOnClosed { plotMap.remove(file) }
            tabs.tabs.add(tab)
            PlotContainer.anchorTo(pane)
        }

        EnvelopeType.infer(file.toPath()).ifPresent { type ->
            try {
                val envelope = type.reader.read(file.toPath())
                val frame = PlotUnWrapper().unWrap(envelope) as FXPlotFrame
                container.plot = frame
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }


    }
}
