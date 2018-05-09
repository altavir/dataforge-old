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

import hep.dataforge.fx.plots.FXPlotManager
import hep.dataforge.fx.plots.group
import hep.dataforge.fx.plots.plot
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.plots.XYFunctionPlot
import hep.dataforge.plots.data.DataPlot
import hep.dataforge.tables.Adapters
import hep.dataforge.tables.ListTable
import hep.dataforge.values.ValueMap
import hep.dataforge.values.Values
import java.util.*


/**
 * @param args the command line arguments
 */

fun main(args: Array<String>) {

    val func = { x: Double -> Math.pow(x, 2.0) }

    val funcPlot = XYFunctionPlot.plot("func", 0.1, 4.0, 200, func)


    val names = arrayOf("myX", "myY", "myXErr", "myYErr")

    val data = ArrayList<Values>()
    data.add(ValueMap.of(names, 0.5, 0.2, 0.1, 0.1))
    data.add(ValueMap.of(names, 1.0, 1.0, 0.2, 0.5))
    data.add(ValueMap.of(names, 3.0, 7.0, 0, 0.5))
    val ds = ListTable.infer(data)

    val dataPlot = DataPlot.plot("data.Plot", Adapters.buildXYAdapter("myX", "myXErr", "myY", "myYErr"), ds)


    val manager = FXPlotManager();
    manager.startGlobal();

    manager.display("test", "testLog") {
        plot(funcPlot)
        config.setNode(MetaBuilder("yAxis").putValue("type", "log"))
        plot(dataPlot)
    }
    manager.display("test", "test") {
        group("sub") {
            plot(funcPlot)
            plot(dataPlot)
        }
    }
    manager.display("test1") {
        plot(funcPlot)
    }

}


