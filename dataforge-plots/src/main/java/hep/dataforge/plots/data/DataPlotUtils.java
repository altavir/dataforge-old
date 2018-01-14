/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.tables.*;
import hep.dataforge.values.Value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alexander Nozik
 */
public class DataPlotUtils {

    public static Table collectXYDataFromPlot(XYPlotFrame frame, boolean visibleOnly) {

        Map<Value, ValueMap.Builder> points = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("x");

        frame.getPlots().list()
                .map(frame::get)
                .filter(pl -> !visibleOnly || pl.getConfig().getBoolean("visible", true))
                .forEach(pl -> {
                    names.add(pl.getTitle());
                    pl.getData().forEach(point -> {
                        Value x = Adapters.getXValue(pl.getAdapter(),point);
                        ValueMap.Builder mdp;
                        if (points.containsKey(x)) {
                            mdp = points.get(x);
                        } else {
                            mdp = new ValueMap.Builder();
                            mdp.putValue("x", x);
                            points.put(x, mdp);
                        }
                        mdp.putValue(pl.getTitle(), Adapters.getYValue(pl.getAdapter(),point));
                    });
                });

        ListTable.Builder res = new ListTable.Builder(MetaTableFormat.forNames(names));
        res.rows(points.values().stream().map(ValueMap.Builder::build).collect(Collectors.toList()));
        return res.build();
    }
}
