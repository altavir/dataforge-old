/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.data.DataFormat;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.ListDataSet;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.data.XYDataAdapter;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Alexander Nozik
 */
public class PlotDataUtils {

    public static DataSet collectXYDataFromPlot(XYPlotFrame frame, boolean visibleOnly) {
        List<XYPlottable> plottables = new ArrayList<>(frame.getAll());

        if (visibleOnly) {
            plottables = plottables.stream().filter((p) -> p.meta().getBoolean("visible", true)).collect(Collectors.toList());
        }

        Map<Value, MapDataPoint> points = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("x");
        
        for (XYPlottable pl : plottables) {
            XYDataAdapter adapter = pl.adapter();

            names.add(pl.getName());
            for (DataPoint point : pl.plotData()) {
                Value x = adapter.getX(point);
                MapDataPoint mdp;
                if (points.containsKey(x)) {
                    mdp = points.get(x);
                } else {
                    mdp = new MapDataPoint();
                    mdp.putValue("x", x);
                    points.put(x, mdp);
                }
                mdp.putValue(pl.getName(), adapter.getY(point));
            }
        }
        ListDataSet res = new ListDataSet(frame.getName(), DataFormat.forNames(8, names));
        res.addAll(points.values());
        return res;
    }
}
