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

import hep.dataforge.context.Context
import hep.dataforge.io.output.Output
import hep.dataforge.names.Name
import hep.dataforge.plots.Plottable
import hep.dataforge.tables.Table
import javafx.beans.binding.ListBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.geometry.Side
import javafx.scene.control.Tab
import tornadofx.*

class OutputContainer(val context: Context) : Fragment() {
    private val stages: ObservableMap<Name, OutputStageContainer> = FXCollections.observableHashMap()
    private val tabs: ObservableList<Tab> = object : ListBinding<Tab>() {
        init {
            bind(stages)
        }

        override fun computeValue(): ObservableList<Tab> {
            return stages.map { Tab(it.key.toUnescaped(), it.value.root) }.observable()
        }
    }

    override val root = tabpane {
        //tabs for each stage
        side = Side.LEFT
        this.tabs.bind(tabs) { it }
    }

    operator fun get(stage: Name, name: Name, type: String): Output {
        return stages.getOrPut(stage) { OutputStageContainer() }[name, type]
    }

    /**
     * Create a new output
     */
    private fun buildOutput(type: String): FXOutput {
        return when {
            type.startsWith(Output.TEXT_TYPE) -> FXTextOutput(context)
            type.startsWith(Plottable.PLOT_TYPE) -> FXPlotOutput(context)
            type.startsWith(Table.TABLE_TYPE) -> FXTableOutput(context)
            else -> FXDumbOutput(context)
        }
    }

    inner class OutputStageContainer : Fragment() {
        private val outputs: ObservableMap<Name, FXOutput> = FXCollections.observableHashMap()

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

        operator fun get(name: Name, type: String): FXOutput {
            return outputs.getOrPut(name) { buildOutput(type) }
        }
    }
}