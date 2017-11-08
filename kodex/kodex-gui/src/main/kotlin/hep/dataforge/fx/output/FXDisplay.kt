package hep.dataforge.fx.output

import hep.dataforge.context.Context
import hep.dataforge.fx.FXPlugin
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import tornadofx.*

/**
 * An interface to produce border panes for content.
 */
interface FXDisplay {
    fun getContainer(stage: String, name: String): BorderPane
}

fun buildDisplay(context: Context): FXDisplay {
    return TabbedFXDisplay().also {
        context.loadFeature("fx", FXPlugin::class.java).show(it)
    }
}

class TabbedFXDisplay : Workspace(), FXDisplay {

    private val stages: Map<String, TabbedStage> = HashMap();
    override fun getContainer(stage: String, name: String): BorderPane {
        return stages.getOrElse(stage) {
            TabbedStage(stage).apply {
                leftDrawer.item(this)
            }
        }.getTab(name).pane
    }

    inner class TabbedStage(val stage: String) : Fragment(stage) {
        private var tabs: MutableMap<String, DisplayTab> = HashMap()
        override val root = TabPane()

//        init {
//            stage = fx.show { stage ->
//                val config = getStageConfig(stage)
//                val width = config.getDouble("width", 800.0)!!
//                val height = config.getDouble("height", 600.0)!!
//                stage.setScene(Scene(tabPane, width, height))
//            }
//            applyStageConfig(this)
//        }

        fun getTab(tabName: String): DisplayTab {
            return tabs.computeIfAbsent(tabName) { DisplayTab(it) }
        }


        inner class DisplayTab(val name: String) {
            val tab: Tab = Tab(name)
            val pane: BorderPane = BorderPane()

            init {
                tab.content = pane
                root.tabs.add(tab)
            }
        }
    }

}
