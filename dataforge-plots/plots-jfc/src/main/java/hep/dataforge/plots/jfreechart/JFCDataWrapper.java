/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.jfreechart;

import hep.dataforge.plots.XYPlottable;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.Value;
import org.jfree.data.xy.AbstractIntervalXYDataset;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexander Nozik
 */
final class JFCDataWrapper extends AbstractIntervalXYDataset {

    private final XYPlottable plottable;
    private final XYAdapter adapter;
    private final Map<Integer, Number> xCache = new ConcurrentHashMap<>();
    private final Map<Integer, Number> yCache = new ConcurrentHashMap<>();
    private int cacheSize = -1;
    private boolean cacheXY;

    public JFCDataWrapper(XYPlottable plottable) {
        this.plottable = plottable;
        adapter = plottable.adapter();
        this.cacheXY = plottable.meta().getBoolean("JFreeChart.cache", false);

        plottable.getConfig().addObserver((String name, Value oldItem, Value newItem) -> {
            switch (name) {
                case "JFreeChart.cache":
                    cacheXY = newItem.booleanValue();
                    clearCache();
                    break;
            }
        }, false);
    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    @Override
    public Comparable getSeriesKey(int i) {
        return plottable.getName();
    }

    private List<DataPoint> getData() {
        return plottable.data();
    }

    @Override
    public int getItemCount(int i) {
        if (cacheXY) {
            if (cacheSize < 0) {
                cacheSize = (int) plottable.dataStream().count();
            }
            return cacheSize;
        } else {
            return (int) plottable.dataStream().count();
        }
    }

    @Override
    public synchronized Number getX(int i, int i1) {
        if (cacheXY) {
            if (xCache.containsKey(i1)) {
                return xCache.get(i1);
            } else {
                Number x = adapter.getX(getData().get(i1)).numberValue();
                xCache.put(i1, x);
                return x;
            }
        } else {
            return adapter.getX(plottable.data().get(i1)).numberValue();
        }
    }

    @Override
    public synchronized Number getY(int i, int i1) {
        if (cacheXY) {
            if (yCache.containsKey(i1)) {
                return yCache.get(i1);
            } else {
                Number y = adapter.getY(getData().get(i1)).numberValue();
                yCache.put(i1, y);
                return y;
            }
        } else {
            return adapter.getY(getData().get(i1)).numberValue();
        }
    }

    @Override
    public Number getStartX(int i, int i1) {
        return adapter.getXLower(getData().get(i1));
    }

    @Override
    public Number getEndX(int i, int i1) {
        return adapter.getXUpper(getData().get(i1));
    }

    @Override
    public Number getStartY(int i, int i1) {
        return adapter.getYLower(getData().get(i1));
    }

    @Override
    public Number getEndY(int i, int i1) {
        return adapter.getYUpper(getData().get(i1));
    }

    public synchronized void clearCache() {
        this.xCache.clear();
        this.yCache.clear();
    }

}
