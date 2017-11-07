package hep.dataforge.fx.output

import hep.dataforge.fx.FXPlugin
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.SimpleConfigurable
import hep.dataforge.names.Named
import javafx.scene.Scene
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.util.*

/**
 * A display where each stage corresponds to the tab pane
 */
class TabbedFXDisplay(private val fx: FXPlugin) : SimpleConfigurable(), FXDisplay {

    private val stages = HashMap<String, TabbedStage>()

    @Synchronized private fun getStage(stageName: String): TabbedStage {
        return (stages as java.util.Map<String, TabbedStage>).computeIfAbsent(stageName){ TabbedStage(it) }
    }

    private fun getStageConfig(name: String): Meta {
        return if (name.isEmpty()) {
            config
        } else {
            Laminate(config.getMetaOrEmpty(name), config)
        }
    }

    private fun applyStageConfig(stage: TabbedStage) {
        val config = getStageConfig(stage.name)

        stage.stage.title = config.getString("title", stage.name)
    }

    override fun getContainer(stage: String, name: String): BorderPane {
        return getStage(stage).getTab(name).pane
    }

    override fun applyConfig(config: Meta) {
        super.applyConfig(config)
        stages.values.forEach{ this.applyStageConfig(it) }
    }

    private inner class TabbedStage(internal var name: String) : Named {
        internal var stage: Stage
        internal var tabPane: TabPane = TabPane()
        internal var tabs: MutableMap<String, DisplayTab> = HashMap()

        init {
            stage = fx.show{ stage ->
                val config = getStageConfig(name)
                val width = config.getDouble("width", 800.0)!!
                val height = config.getDouble("height", 600.0)!!
                stage.setScene(Scene(tabPane, width, height))
            }
            applyStageConfig(this)
        }

        fun getTab(tabName: String): DisplayTab {
            return (tabs as java.util.Map<String, DisplayTab>).computeIfAbsent(tabName){ DisplayTab(it) }
        }

        override fun getName(): String {
            return name
        }

        private inner class DisplayTab(internal var name: String) : Named {
            internal var tab: Tab
            internal var pane: BorderPane

            init {
                tab = Tab(name)
                pane = BorderPane()
                tab.content = pane
                tabPane.tabs.add(tab)
            }

            override fun getName(): String {
                return name
            }
        }
    }
}
