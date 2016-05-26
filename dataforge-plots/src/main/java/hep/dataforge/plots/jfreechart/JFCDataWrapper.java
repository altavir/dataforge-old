/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.jfreechart;

import hep.dataforge.plots.PlotStateListener;
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.tables.XYAdapter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jfree.data.xy.AbstractIntervalXYDataset;

/**
 *
 * @author Alexander Nozik
 */
final class JFCDataWrapper extends AbstractIntervalXYDataset {

    private final XYPlottable plottable;
    private final XYAdapter adapter;
    private final Map<Integer, Number> xCache = new ConcurrentHashMap<>();
    private final Map<Integer, Number> yCache = new ConcurrentHashMap<>();
    private boolean cacheX;
    private boolean cacheY;

    public JFCDataWrapper(XYPlottable plottable) {
        this.plottable = plottable;
        adapter = plottable.adapter();
        this.cacheX = plottable.meta().getBoolean("JFreeChart.cacheX", false);
        this.cacheY = plottable.meta().getBoolean("JFreeChart.cacheY", false);
        //Do not notify plot here since it is automatical
        plottable.addListener(new PlotStateListener() {
            @Override
            public void notifyDataChanged(String name) {
                clearCache();
            }

            @Override
            public void notifyConfigurationChanged(String name) {
                clearCache();
                cacheX = plottable.meta().getBoolean("JFreeChart.cacheX", false);
                cacheY = plottable.meta().getBoolean("JFreeChart.cacheY", false);
            }
        });
    }

//    public JFCDataWrapper(XYPlottable plottable, boolean cacheX, boolean cacheY) {
//        this(plottable);
//        this.cacheX = cacheX;
//        this.cacheY = cacheY;
//    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    @Override
    public Comparable getSeriesKey(int i) {
        return plottable.getName();
    }

    @Override
    public int getItemCount(int i) {
        return (int) plottable.dataStream().count();
    }

    @Override
    public synchronized Number getX(int i, int i1) {
        if (cacheX) {
            if (xCache.containsKey(i1)) {
                return xCache.get(i1);
            } else {
                Number x = adapter.getX(plottable.data().get(i1)).numberValue();
                xCache.put(i1, x);
                return x;
            }
        } else {
            return adapter.getX(plottable.data().get(i1)).numberValue();
        }
    }

    @Override
    public synchronized Number getY(int i, int i1) {
        if (cacheY) {
            if (yCache.containsKey(i1)) {
                return yCache.get(i1);
            } else {
                Number y = adapter.getY(plottable.data().get(i1)).numberValue();
                yCache.put(i1, y);
                return y;
            }
        } else {
            return adapter.getY(plottable.data().get(i1)).numberValue();
        }
    }

    @Override
    public Number getStartX(int i, int i1) {
        return adapter.getXLower(plottable.data().get(i1));
    }

    @Override
    public Number getEndX(int i, int i1) {
        return adapter.getXUpper(plottable.data().get(i1));
    }

    @Override
    public Number getStartY(int i, int i1) {
        return adapter.getYLower(plottable.data().get(i1));
    }

    @Override
    public Number getEndY(int i, int i1) {
        return adapter.getYUpper(plottable.data().get(i1));
    }

    public synchronized void clearCache() {
        this.xCache.clear();
        this.yCache.clear();
    }

}
