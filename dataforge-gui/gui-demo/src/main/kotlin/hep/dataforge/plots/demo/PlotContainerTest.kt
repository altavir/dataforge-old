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

import hep.dataforge.fx.output.FXOutputManager
import hep.dataforge.fx.plots.group
import hep.dataforge.kodex.buildContext
import hep.dataforge.kodex.configure
import hep.dataforge.plots.XYFunctionPlot
import hep.dataforge.plots.data.DataPlot
import hep.dataforge.plots.jfreechart.JFreeChartPlugin
import hep.dataforge.plots.output.plot
import hep.dataforge.tables.Adapters
import hep.dataforge.tables.ListTable
import hep.dataforge.values.ValueMap
import hep.dataforge.values.Values
import java.util.*


/**
 * @param args the command line arguments
 */

fun main(args: Array<String>) {

    val context = buildContext("TEST", JFreeChartPlugin::class.java) {
        output = FXOutputManager()
    }

    val func = { x: Double -> Math.pow(x, 2.0) }

    val funcPlot = XYFunctionPlot.plot("func", 0.1, 4.0, 200, func)


    val names = arrayOf("myX", "myY", "myXErr", "myYErr")

    val data = ArrayList<Values>()
    data.add(ValueMap.of(names, 0.5, 0.2, 0.1, 0.1))
    data.add(ValueMap.of(names, 1.0, 1.0, 0.2, 0.5))
    data.add(ValueMap.of(names, 3.0, 7.0, 0, 0.5))
    val ds = ListTable.infer(data)

    val dataPlot = DataPlot.plot("data.Plot", Adapters.buildXYAdapter("myX", "myXErr", "myY", "myYErr"), ds)

    context.plot("test"){
        configure {
            "yAxis" to {
                "type" to "log"
            }
        }
        +funcPlot
        +dataPlot
        group("sub"){
            +funcPlot
            +dataPlot
        }
    }

    context.plot("test1"){
        +funcPlot
    }

}


