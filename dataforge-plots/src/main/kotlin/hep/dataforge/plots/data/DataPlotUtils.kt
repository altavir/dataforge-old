/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data

import hep.dataforge.kodex.toList
import hep.dataforge.plots.XYPlotFrame
import hep.dataforge.tables.*
import hep.dataforge.values.Value
import java.util.*

/**
 * @author Alexander Nozik
 */
object DataPlotUtils {

    fun collectXYDataFromPlot(frame: XYPlotFrame, visibleOnly: Boolean): Table {

        val points = LinkedHashMap<Value, ValueMap.Builder>()
        val names = ArrayList<String>()
        names.add("x")

        frame.plots.list()
                .map { frame[it] }
                .filter { pl -> !visibleOnly || pl.getConfig().getBoolean("visible", true) }
                .forEach { pl ->
                    names.add(pl.title)
                    pl.data.forEach { point ->
                        val x = Adapters.getXValue(pl.adapter, point)
                        val mdp: ValueMap.Builder = points.getOrPut(x) {
                            ValueMap.Builder().apply { putValue("x", x) }
                        }
                        mdp.putValue(pl.title, Adapters.getYValue(pl.adapter, point))
                    }
                }

        val res = ListTable.Builder(MetaTableFormat.forNames(names))
        res.rows(points.values.stream().map { it.build() }.toList())
        return res.build()
    }
}
