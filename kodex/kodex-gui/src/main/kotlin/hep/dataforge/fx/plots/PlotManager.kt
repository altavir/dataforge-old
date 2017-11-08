package hep.dataforge.fx.plots

import hep.dataforge.context.BasicPlugin
import hep.dataforge.context.PluginDef
import hep.dataforge.fx.output.FXDisplay
import hep.dataforge.fx.output.buildDisplay
import hep.dataforge.plots.PlotFrame
import hep.dataforge.plots.PlotPlugin
import hep.dataforge.plots.jfreechart.JFreeChartFrame
import javafx.stage.Stage
import tornadofx.*

const val DEFAULT_STAGE_NAME = "";
const val DEFAULT_PLOT_NAME = "";


@PluginDef(name = "plots", group = "hep.dataforge", dependsOn = arrayOf("hep.dataforge:fx"), info = "Basic plottiong plugin")
class PlotManager : BasicPlugin(), PlotPlugin {
    private val stages: MutableMap<String, MutableMap<String, PlotContainer>> = HashMap()

    /**
     * Plot frame factory
     */
    var frameFactory: () -> PlotFrame = {
        try {
            JFreeChartFrame()
        } catch (ex: Exception) {
            throw RuntimeException("Plot frame factory not configured");
        }
    };

    private val display: FXDisplay by lazy {
        buildDisplay(context)
    };

    override fun getPlotFrame(stage: String, name: String): PlotFrame {
        return stages
                .computeIfAbsent(stage) { HashMap() }
                .computeIfAbsent(name) {
                    PlotContainer(frameFactory()).apply {
                        display(stage, name, this)
                    }
                }
                .frame
    }

    private fun display(stage: String, name: String, container: PlotContainer) {
        runLater {
            val pane = display.getContainer(stage, name)
            pane.center = container.root;
            (pane.scene.window as? Stage)?.show()
        }
    }

    override fun hasPlotFrame(stage: String, name: String): Boolean {
        return stages.containsKey(stage) && (stages[stage]?.containsKey(name) ?: false);
    }

    fun display(stage: String = DEFAULT_STAGE_NAME, name: String = DEFAULT_PLOT_NAME, action: PlotFrame.() -> Unit): PlotFrame {
        return getPlotFrame(stage, name).apply(action)
    }

    fun display(stage: String = DEFAULT_STAGE_NAME, name: String = DEFAULT_PLOT_NAME, frame: PlotFrame): PlotFrame {
        return stages
                .computeIfAbsent(stage) { HashMap() }
                .computeIfAbsent(name) {
                    PlotContainer(frame).apply {
                        display(stage, name, this)
                    }
                }.frame
    }

}