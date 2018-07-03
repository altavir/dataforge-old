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
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.KMetaBuilder
import hep.dataforge.meta.buildMeta
import hep.dataforge.plots.FakePlotFrame
import hep.dataforge.plots.PlotFrame
import hep.dataforge.plots.Plottable

interface PlotOutput : Output, Configurable {
    val frame: PlotFrame

    companion object {
        const val PLOT_TYPE = "hep.dataforge.plot"
    }
}

fun Context.plot(stage: String, name: String, plottable: Plottable, transform: KMetaBuilder.() -> Unit = {}) {
    output.get(stage, name, PlotOutput.PLOT_TYPE).render(plottable, buildMeta("frame", transform))
}

fun Context.plot(stage: String = "", name: String, plottables: Iterable<Plottable>, transform: KMetaBuilder.() -> Unit = {}) {
    output.get(stage, name, PlotOutput.PLOT_TYPE).render(plottables, buildMeta("frame", transform))
}

fun Context.plot(name: String, stage: String = "", action: PlotFrame.() -> Unit) {
    val frame = FakePlotFrame().apply(action)
    output.get(stage, name, PlotOutput.PLOT_TYPE).render(frame.plots, frame.config)
}

//@JvmOverloads
//fun Context.getPlotFrame(name: String, stage: String = "", meta: Meta = Meta.empty()): PlotFrame {
//    return (output[name, stage, PlotOutput.PLOT_TYPE] as PlotOutput).apply { configure(meta) }.frame
//}
//
//fun Context.getPlotFrame(name: String, stage: String = "", transform: KMetaBuilder.() -> Unit): PlotFrame {
//    return (output[name, stage, PlotOutput.PLOT_TYPE] as PlotOutput).configure(transform).frame
//}