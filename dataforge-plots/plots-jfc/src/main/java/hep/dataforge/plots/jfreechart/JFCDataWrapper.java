/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.jfreechart;

import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.plots.Plottable;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.Value;
import org.jfree.data.xy.AbstractIntervalXYDataset;

import java.util.List;

/**
 * @author Alexander Nozik
 */
final class JFCDataWrapper extends AbstractIntervalXYDataset {

    private final Plottable plottable;

    private XYAdapter adapter;
    private List<DataPoint> data;
    private Meta query = Meta.empty();

    public JFCDataWrapper(Plottable plottable) {
        this.plottable = plottable;
        adapter = XYAdapter.from(plottable.getAdapter());
    }

    private synchronized List<DataPoint> getData() {
        if (data == null) {
            data = plottable.getData(query);
        }
        return data;
    }

    private synchronized DataPoint getAt(int i) {
        return getData().get(i);
    }

    @Override
    public int getSeriesCount() {
        return adapter.getYCount();
    }

    @Override
    public Comparable getSeriesKey(int i) {
        if (getSeriesCount() == 1) {
            return plottable.getName();
        } else {
            return Name.joinString(plottable.getName(), adapter.getYTitle(i));
        }
    }

    @Override
    public Number getStartX(int i, int i1) {
        return adapter.getXLower(getAt(i1));
    }

    @Override
    public Number getEndX(int i, int i1) {
        return adapter.getXUpper(getAt(i1));
    }

    @Override
    public Number getStartY(int i, int i1) {
        return adapter.getYLower(getAt(i1), i);
    }

    @Override
    public Number getEndY(int i, int i1) {
        return adapter.getYUpper(getAt(i1), i);
    }

    @Override
    public int getItemCount(int i) {
        return getData().size();
    }

    @Override
    public Number getX(int i, int i1) {
        return transform(adapter.getX(getAt(i1)));
    }

    private Number transform(Value value) {
        if (value.isNull()) {
            return null;
        } else {
            return value.doubleValue();
        }
    }

    @Override
    public Number getY(int i, int i1) {
        return transform(adapter.getY(getAt(i1), i));
    }

    public void invalidateData() {
        this.data = null;
    }

    //    private final Map<Integer, Number> xCache = new ConcurrentHashMap<>();
//    private final Map<Integer, Number> yCache = new ConcurrentHashMap<>();
//    private int cacheSize = -1;
//    private boolean cacheXY;

//    public JFCDataWrapper(XYPlottable plottable) {
//        this.plottable = plottable;
//        adapter = plottable.getAdapter();
//        this.cacheXY = plottable.meta().getBoolean("JFreeChart.cache", false);
//
//        plottable.getConfig().addObserver((String name, Value oldItem, Value newItem) -> {
//            switch (name) {
//                case "JFreeChart.cache":
//                    cacheXY = newItem.booleanValue();
//                    clearCache();
//                    break;
//            }
//        }, false);
//    }
//
//    @Override
//    public int getSeriesCount() {
//        return adapter.getYCount();
//    }
//
//    @Override
//    public Comparable getSeriesKey(int i) {
////        if (getSeriesCount() == 1) {
////            return plottable.getName();
////        } else {
////            return Name.joinString(plottable.getName(), adapter.getY())
////        }
//        return plottable.getName();
//    }
//
//    @Override
//    public int getItemCount(int i) {
//        if (cacheXY) {
//            if (cacheSize < 0) {
//                cacheSize = (int) plottable.dataStream().count();
//            }
//            return cacheSize;
//        } else {
//            return (int) plottable.dataStream().count();
//        }
//    }
//
//    @Override
//    public synchronized Number getX(int i, int i1) {
//        if (cacheXY) {
//            if (xCache.containsKey(i1)) {
//                return xCache.get(i1);
//            } else {
//                Number x = adapter.getX(plottable.getPoint(i1)).numberValue();
//                xCache.put(i1, x);
//                return x;
//            }
//        } else {
//            return adapter.getX(plottable.getPoint(i1)).numberValue();
//        }
//    }
//
//    @Override
//    public synchronized Number getY(int i, int i1) {
//        if (cacheXY) {
//            if (yCache.containsKey(i1)) {
//                return yCache.get(i1);
//            } else {
//                Number y = adapter.getY(plottable.getPoint(i1)).numberValue();
//                yCache.put(i1, y);
//                return y;
//            }
//        } else {
//            return adapter.getY(plottable.getPoint(i1)).numberValue();
//        }
//    }
//
//    @Override
//    public Number getStartX(int i, int i1) {
//        return adapter.getXLower(plottable.getPoint(i1));
//    }
//
//    @Override
//    public Number getEndX(int i, int i1) {
//        return adapter.getXUpper(plottable.getPoint(i1));
//    }
//
//    @Override
//    public Number getStartY(int i, int i1) {
//        return adapter.getYLower(plottable.getPoint(i1));
//    }
//
//    @Override
//    public Number getEndY(int i, int i1) {
//        return adapter.getYUpper(plottable.getPoint(i1));
//    }
//
//    public synchronized void clearCache() {
//        this.xCache.clear();
//        this.yCache.clear();
//    }

}
