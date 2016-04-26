/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.tables.TableFormat;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.MapPoint;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import hep.dataforge.tables.Table;

/**
 *
 * @author Alexander Nozik
 */
public class PlotDataUtils {

    public static Table collectXYDataFromPlot(XYPlotFrame frame, boolean visibleOnly) {
        List<XYPlottable> plottables = new ArrayList<>(frame.getAll());

        if (visibleOnly) {
            plottables = plottables.stream().filter((p) -> p.meta().getBoolean("visible", true)).collect(Collectors.toList());
        }

        Map<Value, DataPoint> points = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("x");
        
        for (XYPlottable pl : plottables) {
            XYAdapter adapter = pl.adapter();

            names.add(pl.getName());
            for (DataPoint point : pl.plotData()) {
                Value x = adapter.getX(point);
                MapPoint.Builder mdp;
                if (points.containsKey(x)) {
                    mdp = new MapPoint.Builder(points.get(x));
                } else {
                    mdp = new MapPoint.Builder();
                    mdp.putValue("x", x);
                    points.put(x, mdp.build());
                }
                mdp.putValue(pl.getName(), adapter.getY(point));
            }
        }
        ListTable.Builder res = new ListTable.Builder(TableFormat.fixedWidth(8, names));
        res.addRows(points.values());
        return res.build();
    }
}
