package hep.dataforge.plots.demo

import hep.dataforge.fx.plots.PlotManager
import hep.dataforge.plots.PlotFrame
import hep.dataforge.plots.data.DataPlot
import hep.dataforge.plots.data.XYFunctionPlot
import hep.dataforge.tables.Adapters
import hep.dataforge.tables.ListTable
import hep.dataforge.tables.ValueMap
import hep.dataforge.values.Values
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

/**
 * @param args the command line arguments
 */

fun main(args: Array<String>) {

    val func = { x: Double -> Math.pow(x, 2.0) }

    val funcPlot = XYFunctionPlot.plotFunction("func", func, 0.1, 4.0, 200)


    val names = arrayOf("myX", "myY", "myXErr", "myYErr")

    val data = ArrayList<Values>()
    data.add(ValueMap.of(names, 0.5, 0.2, 0.1, 0.1))
    data.add(ValueMap.of(names, 1.0, 1.0, 0.2, 0.5))
    data.add(ValueMap.of(names, 3.0, 7.0, 0, 0.5))
    val ds = ListTable(data)

    val dataPlot = DataPlot.plot("dataPlot", Adapters.buildXYAdapter("myX", "myXErr", "myY", "myYErr"), ds)


    val manager = PlotManager();
    manager.startGlobal();


    val frame = manager.display("test", "before") {
        add(dataPlot)
        //add(DataPlot.plot("dataPlot2", XYAdapter("myX", "myXErr", "myY", "myYErr"), ds))
        add(funcPlot)
    }

    val baos = ByteArrayOutputStream();

    ObjectOutputStream(baos).use {
        it.writeObject(frame)
    }

    val bais = ByteArrayInputStream(baos.toByteArray());
    val restored: PlotFrame = ObjectInputStream(bais).readObject() as PlotFrame
    manager.display("test", "after", restored)

}