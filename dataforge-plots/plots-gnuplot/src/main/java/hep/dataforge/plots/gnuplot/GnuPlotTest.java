package hep.dataforge.plots.gnuplot;

import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.DataPlot;
import hep.dataforge.plots.data.XYFunctionPlot;
import hep.dataforge.tables.Adapters;
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.ValueMap;
import hep.dataforge.values.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by darksnake on 11-May-17.
 */
public class GnuPlotTest {
    public static void main(String[] args) {
        GnuPlotFrame frame = new GnuPlotFrame();
        frame.configureValue("gnuplot.path", "C:\\Program Files\\gnuplot\\bin\\gnuplot.exe");

        Function<Double, Double> func = (x1) -> x1 * x1;

        XYFunctionPlot funcPlot = XYFunctionPlot.plotFunction("func", 0.1, 4, 200, func);

        frame.add(funcPlot);

        String[] names = {"myX", "myY", "myYErr"};

        List<Values> data = new ArrayList<>();
        data.add(ValueMap.of(names, 0.5d, 0.2, 0.1));
        data.add(ValueMap.of(names, 1d, 1d, 0.5));
        data.add(ValueMap.of(names, 3d, 7d, 0.5));
        Table ds = new ListTable(data);

        DataPlot dataPlot = DataPlot.plot("dataPlot", Adapters.buildXYAdapter("myX", "myY", "myYErr"), ds);

        frame.getConfig().setNode(new MetaBuilder("yAxis").putValue("type", "log"));

        frame.add(dataPlot);

        frame.plot();

        System.out.println(frame.getCommands());
    }
}
