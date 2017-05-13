package hep.dataforge.plots.gnuplot;

import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.PlottableData;
import hep.dataforge.plots.data.PlottableXYFunction;
import hep.dataforge.tables.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by darksnake on 11-May-17.
 */
public class GnuPlotTest {
    public static void main(String[] args){
        GnuPlotFrame frame  = new GnuPlotFrame();
        frame.configureValue("gnuplot.path", "C:\\Program Files\\gnuplot\\bin\\gnuplot.exe");

        Function<Double,Double> func = (x1) -> x1 * x1;

        PlottableXYFunction funcPlot = PlottableXYFunction.plotFunction("func", func, 0.1, 4, 200);

        frame.add(funcPlot);

        String[] names = {"myX", "myY", "myXErr", "myYErr"};

        List<DataPoint> data = new ArrayList<>();
        data.add(new MapPoint(names, 0.5d, 0.2, 0.1, 0.1));
        data.add(new MapPoint(names, 1d, 1d, 0.2, 0.5));
        data.add(new MapPoint(names, 3d, 7d, 0, 0.5));
        Table ds = new ListTable(data);

        PlottableData dataPlot = PlottableData.plot("dataPlot", new XYAdapter("myX", "myXErr", "myY", "myYErr"), ds);

        frame.getConfig().setNode(new MetaBuilder("yAxis").putValue("type", "log"));

        frame.add(dataPlot);

        frame.plot();

        System.out.println(frame.getCommands());
    }
}