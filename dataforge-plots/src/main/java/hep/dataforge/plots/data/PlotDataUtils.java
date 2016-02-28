/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.data.Format;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.ListPointSet;
import hep.dataforge.data.MapPoint;
import hep.dataforge.data.XYAdapter;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import hep.dataforge.data.PointSet;

/**
 *
 * @author Alexander Nozik
 */
public class PlotDataUtils {

    public static PointSet collectXYDataFromPlot(XYPlotFrame frame, boolean visibleOnly) {
        List<XYPlottable> plottables = new ArrayList<>(frame.getAll());

        if (visibleOnly) {
            plottables = plottables.stream().filter((p) -> p.meta().getBoolean("visible", true)).collect(Collectors.toList());
        }

        Map<Value, MapPoint> points = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("x");
        
        for (XYPlottable pl : plottables) {
            XYAdapter adapter = pl.adapter();

            names.add(pl.getName());
            for (DataPoint point : pl.plotData()) {
                Value x = adapter.getX(point);
                MapPoint mdp;
                if (points.containsKey(x)) {
                    mdp = points.get(x);
                } else {
                    mdp = new MapPoint();
                    mdp.putValue("x", x);
                    points.put(x, mdp);
                }
                mdp.putValue(pl.getName(), adapter.getY(point));
            }
        }
        ListPointSet res = new ListPointSet(frame.getName(), Format.forNames(8, names));
        res.addAll(points.values());
        return res;
    }
}
