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
 * Wrapper for plottable. Multiple xs are not allowed
 *
 * @author Alexander Nozik
 */
final class JFCDataWrapper extends AbstractIntervalXYDataset {

    private Plottable plottable;

    private XYAdapter adapter;
    private List<DataPoint> data;
    private Meta query = Meta.empty();
    private Integer index = 0;


    public JFCDataWrapper(Plottable plottable) {
        this.plottable = plottable;
        adapter = XYAdapter.from(plottable.getAdapter());
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Plottable getPlottable() {
        return plottable;
    }

    public void setPlottable(Plottable plottable) {
        if (this.plottable != plottable) {
            this.plottable = plottable;
            invalidateData();
        }
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
}
