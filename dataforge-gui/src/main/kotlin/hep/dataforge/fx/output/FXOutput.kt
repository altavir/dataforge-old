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
import hep.dataforge.description.Descriptors
import hep.dataforge.fx.plots.PlotContainer
import hep.dataforge.fx.table.TableDisplay
import hep.dataforge.io.output.Output
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import hep.dataforge.plots.PlotFactory
import hep.dataforge.plots.PlotFrame
import hep.dataforge.plots.Plottable
import hep.dataforge.plots.output.PlotOutput
import hep.dataforge.tables.Table
import hep.dataforge.useValue
import tornadofx.*

/**
 * An output container represented as FX fragment. The view is initialized lazily to avoid problems with toolkit initialization.
 */
abstract class FXOutput(override val context: Context) : Output {
    abstract val view: Fragment
}

/**
 * A specialized output for tables. Pushing new table replaces the old one
 */
class FXTableOutput(context: Context) : FXOutput(context) {
    val tableDisplay: TableDisplay = TableDisplay()

    override val view: Fragment = object : Fragment() {
        override val root = borderpane {
            center = tableDisplay.root
        }
    }


    override fun render(obj: Any, meta: Meta) {
        if (obj is Table) {
            runLater {
                tableDisplay.table = obj
            }
        } else {
            logger.error("Can't represent ${obj.javaClass} as Table")
        }
    }

}

class FXPlotOutput(context: Context, meta: Meta = Meta.empty()) : FXOutput(context), PlotOutput, Configurable {

    override val frame: PlotFrame  by lazy { context.get<PlotFactory>().build(meta) }

    val container: PlotContainer by lazy { PlotContainer(frame) }

    override val view: Fragment by lazy {
        object : Fragment() {
            override val root = borderpane {
                center = container.root
            }
        }
    }

    override fun getConfig(): Configuration = frame.config

    override fun render(obj: Any, meta: Meta) {
        if (!meta.isEmpty) {
            if (!frame.config.isEmpty) {
                logger.warn("Overriding non-empty frame configuration")
            }
            frame.configure(meta)
            // Use descriptor hidden field to update root plot container description
            meta.useValue("@descriptor") {
                frame.plots.descriptor = Descriptors.forName("plot", it.string)
            }
        }
        runLater {
            when (obj) {
                is PlotFrame -> {
                    frame.configure(obj.config)
                    frame.plots.configure(obj.plots.config)
                    frame.plots.descriptor = obj.plots.descriptor
                    frame.addAll(obj.plots.children)
                }
                is Plottable -> {
                    frame.add(obj)
                }
                is Iterable<*> -> {
                    frame.addAll(obj.filterIsInstance<Plottable>())
                }
                else -> {
                    logger.error("Can't represent ${obj.javaClass} as Plottable")
                }
            }
        }
    }
}