/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.tables.*;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Alexander Nozik
 */
public class PlotDataUtils {

    public static Table collectXYDataFromPlot(XYPlotFrame frame, boolean visibleOnly) {

        Map<Value, ValueMap.Builder> points = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("x");

        frame.getPlots().list()
                .map(frame::get)
                .filter(pl -> !visibleOnly || pl.meta().getBoolean("visible", true))
                .forEach(pl -> {
                    XYAdapter adapter = XYAdapter.from(pl.getAdapter());

                    names.add(pl.getName());
                    pl.getData().forEach(point -> {
                        Value x = adapter.getX(point);
                        ValueMap.Builder mdp;
                        if (points.containsKey(x)) {
                            mdp = points.get(x);
                        } else {
                            mdp = new ValueMap.Builder();
                            mdp.putValue("x", x);
                            points.put(x, mdp);
                        }
                        mdp.putValue(pl.getName(), adapter.getY(point));
                    });
                });

        ListTable.Builder res = new ListTable.Builder(MetaTableFormat.forNames(names));
        res.rows(points.values().stream().map(ValueMap.Builder::build).collect(Collectors.toList()));
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
    public static PlottableGroup<DataPlot> buildGroup(String xName, Collection<String> yNames, Stream<Values> source) {
        List<Values> points = source.collect(Collectors.toList());
        List<DataPlot> plottables = yNames.stream().map(yName -> {
            DataPlot pl = new DataPlot(yName);
            pl.setAdapter(new XYAdapter(xName, yName));
            return pl;
        }).collect(Collectors.toList());
        plottables.forEach(pl -> pl.setData(points));

        return new PlottableGroup<DataPlot>(plottables);
    }

}
