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

package hep.dataforge.plots.output

import hep.dataforge.context.Context
import hep.dataforge.io.output.Output
import hep.dataforge.kodex.KMetaBuilder
import hep.dataforge.kodex.buildMeta
import hep.dataforge.meta.Meta
import hep.dataforge.plots.PlotFrame
import hep.dataforge.plots.PlotPlugin
import hep.dataforge.plots.Plottable

class PlotOutput(override val context: Context, val frameFactory: () -> PlotFrame) : Output {
    private val frame: PlotFrame? = null

    override fun render(obj: Any, meta: Meta) {
        when (obj) {
            is Plottable -> {
                (this.frame ?: frameFactory()).apply {
                    //TODO add warning on config update
                    configure(meta)
                }.add(obj)
            }
            else -> context.logger.error("Trying to render non-plottable object with plot renderer. No output.")
        }
    }

}

fun Context.getPlotFrame(name: String, stage: String = "", meta: Meta = Meta.empty()): PlotFrame {
    //FIXME replace by output manager
    return this.get<PlotPlugin>().getPlotFrame(stage, name, meta)
}

fun Context.getPlotFrame(name: String, stage: String = "", transform: KMetaBuilder.() -> Unit): PlotFrame {
    return this.get<PlotPlugin>().getPlotFrame(stage, name, buildMeta(transform = transform))
}