/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.plots.Plottable;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.tables.*;
import hep.dataforge.values.Value;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Alexander Nozik
 */
public class PlotDataUtils {

    public static Table collectXYDataFromPlot(XYPlotFrame frame, boolean visibleOnly) {
        List<Plottable> plottables = new ArrayList<>(frame.plottables());

        if (visibleOnly) {
            plottables = plottables.stream().filter((p) -> p.meta().getBoolean("visible", true)).collect(Collectors.toList());
        }

        Map<Value, MapPoint.Builder> points = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("x");

        for (Plottable pl : plottables) {
            XYAdapter adapter = XYAdapter.from(pl.getAdapter());

            names.add(pl.getName());
            pl.getData().stream().forEach(point -> {
                Value x = adapter.getX(point);
                MapPoint.Builder mdp;
                if (points.containsKey(x)) {
                    mdp = points.get(x);
                } else {
                    mdp = new MapPoint.Builder();
                    mdp.putValue("x", x);
                    points.put(x, mdp);
                }
                mdp.putValue(pl.getName(), adapter.getY(point));
            });

        }
        ListTable.Builder res = new ListTable.Builder(TableFormat.forNames(names));
        res.rows(points.values().stream().map(p -> p.build()).collect(Collectors.toList()));
        return res.build();
    }

    /**
     * Build a group from single point stream but multiple y-s
     *
     * @param xName
     * @param yNames
     * @param source
     * @return
     */
    public static PlottableGroup<PlottableData> buildGroup(String xName, Collection<String> yNames, Stream<DataPoint> source) {
        List<DataPoint> points = source.collect(Collectors.toList());
        List<PlottableData> plottables = yNames.stream().map(yName -> {
            PlottableData pl = new PlottableData(yName);
            pl.setAdapter(new XYAdapter(xName, yName));
            return pl;
        }).collect(Collectors.toList());
        plottables.forEach(pl-> pl.setData(points));

        return new PlottableGroup<PlottableData>(plottables);
    }

}
