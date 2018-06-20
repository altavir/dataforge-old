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
import hep.dataforge.fx.dfIconView
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
import tornadofx.*

abstract class FXOutput(override val context: Context) : Fragment(icon = dfIconView), Output

class FXTableOutput(context: Context) : FXOutput(context) {
    override val root = scrollpane()

    override fun render(obj: Any, meta: Meta) {
        if (obj is Table) {
            root.content = TableDisplay(obj).root
        } else {
            logger.error("Can't represent ${obj.javaClass} as Table")
        }
    }

}

class FXPlotOutput(context: Context) : FXOutput(context), PlotOutput, Configurable {

    override val frame: PlotFrame  by lazy { context.get<PlotFactory>().build(Meta.empty()) }

    val container: PlotContainer by lazy { PlotContainer(frame) }

    override val root = borderpane {
        center = container.root
    }

    override fun getConfig(): Configuration = frame.config


    override fun render(obj: Any, meta: Meta) {
        if (!meta.isEmpty) {
            if (!frame.config.isEmpty) {
                logger.warn("Overriding non-empty frame configuration")
            }
            frame.configure(meta)
        }
        when (obj) {
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