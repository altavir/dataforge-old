/*
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.plots.demo

import hep.dataforge.kodex.fx.plots.PlotContainer
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.plots.data.PlotData
import hep.dataforge.plots.data.PlotXYFunction
import hep.dataforge.plots.jfreechart.JFreeChartFrame
import hep.dataforge.tables.ListTable
import hep.dataforge.tables.ValueMap
import hep.dataforge.tables.XYAdapter
import hep.dataforge.values.Values
import java.util.*


/**
 * @param args the command line arguments
 */

fun main(args: Array<String>) {
    val frame = JFreeChartFrame()

    val func = { x: Double -> Math.pow(x, 2.0) }

    val funcPlot = PlotXYFunction.plotFunction("func", func, 0.1, 4.0, 200)

    frame.add(funcPlot)

    val names = arrayOf("myX", "myY", "myXErr", "myYErr")

    val data = ArrayList<Values>()
    data.add(ValueMap.of(names, 0.5, 0.2, 0.1, 0.1))
    data.add(ValueMap.of(names, 1.0, 1.0, 0.2, 0.5))
    data.add(ValueMap.of(names, 3.0, 7.0, 0, 0.5))
    val ds = ListTable(data)

    val dataPlot = PlotData.plot("dataPlot", XYAdapter("myX", "myXErr", "myY", "myYErr"), ds)

    frame.config.setNode(MetaBuilder("yAxis").putValue("type", "log"))

    frame.add(dataPlot)

    PlotContainer.display(frame)
}


