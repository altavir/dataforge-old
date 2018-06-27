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

package hep.dataforge.fx.output

import hep.dataforge.context.BasicPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginTag
import hep.dataforge.fx.FXPlugin
import hep.dataforge.fx.dfIconView
import hep.dataforge.io.OutputManager
import hep.dataforge.io.output.Output
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.plots.output.PlotOutput
import hep.dataforge.tables.Table
import javafx.beans.binding.ListBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.geometry.Side
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import tornadofx.*

class OutputContainer(val context: Context, val meta: Meta) : Fragment(title = "[${context.name}] DataForge output container", icon = dfIconView) {

    private val stages: ObservableMap<Name, OutputStageContainer> = FXCollections.observableHashMap()

    private val tabList: ObservableList<Tab> = object : ListBinding<Tab>() {
        init {
            bind(stages)
        }

        override fun computeValue(): ObservableList<Tab> {
            return stages.map {
                Tab(it.key.toUnescaped(), it.value.root).apply {
                    isClosable = false
                }
            }.observable()
        }
    }

    override val root = tabpane {
        //tabs for each stage
        side = Side.LEFT
        tabs.bind(tabList) { it }
    }

    private fun buildStageContainer(): OutputStageContainer {
        return if (meta.getBoolean("treeStage", false)) {
            TreeStageContainer()
        } else {
            TabbedStageContainer()
        }
    }

    operator fun get(stage: Name, name: Name, type: String): Output {
        return (stages[stage] ?: buildStageContainer().also { runLater { stages[stage] = it } })[name, type]
    }

    /**
     * Create a new output
     */
    private fun buildOutput(type: String): FXOutput {
        return when {
            type.startsWith(Output.TEXT_MODE) -> FXTextOutput(context)
            type.startsWith(PlotOutput.PLOT_TYPE) -> FXPlotOutput(context)
            type.startsWith(Table.TABLE_TYPE) -> FXTableOutput(context)
            else -> FXDumbOutput(context)
        }
    }

    private abstract inner class OutputStageContainer : Fragment() {
        val outputs: ObservableMap<Name, FXOutput> = FXCollections.observableHashMap()

        operator fun get(name: Name, type: String): FXOutput {
            return outputs[name] ?: buildOutput(type).also { runLater { outputs[name] = it } }
        }
    }

    private inner class TreeStageContainer : OutputStageContainer() {
        override val root = borderpane {
            left {
                // name list
                //TODO replace by tree
                listview<Name> {
                    items = object : ListBinding<Name>() {
                        init {
                            bind(outputs)
                        }

                        override fun computeValue(): ObservableList<Name> {
                            return outputs.keys.toList().observable()
                        }
                    }
                    onUserSelect {
                        this@borderpane.center = outputs[it]!!.root
                    }
                }
            }
        }
    }

    private inner class TabbedStageContainer : OutputStageContainer() {

        private val outputList: ObservableList<Tab> = object : ListBinding<Tab>() {
            init {
                bind(outputs)
            }

            override fun computeValue(): ObservableList<Tab> {
                return outputs.map {
                    Tab(it.key.toUnescaped(), it.value.root).apply {
                        isClosable = false
                    }
                }.observable()
            }
        }

        override val root = tabpane {
            //tabs for each output
            side = Side.TOP
            tabs.bind(outputList) { it }
        }
    }
}

class FXOutputManager(meta: Meta = Meta.empty(), viewConsumer: Context.(OutputContainer) -> Unit = { get<FXPlugin>().display(it) }) : OutputManager, BasicPlugin(meta) {

    override val tag = PluginTag(name = "output.fx", dependsOn = *arrayOf("hep.dataforge:fx"))

    override fun attach(context: Context) {
        super.attach(context)
        //Check if FX toolkit is started
        context.load<FXPlugin>()
    }

    private val container: OutputContainer by lazy {
        OutputContainer(context, meta).also { viewConsumer.invoke(context, it) }
    }

    val root: UIComponent
        get() = container

    override val outputModes: Collection<String> = listOf(Output.TEXT_MODE, PlotOutput.PLOT_TYPE, Table.TABLE_TYPE)

    override fun get(stage: Name, name: Name, mode: String): Output {
        return container[name, stage, mode]
    }

    companion object {

        /**
         * Display in existing BorderPane
         */
        fun display(pane: BorderPane, meta: Meta = Meta.empty()): FXOutputManager = FXOutputManager(meta) { pane.center = it.root }
    }
}