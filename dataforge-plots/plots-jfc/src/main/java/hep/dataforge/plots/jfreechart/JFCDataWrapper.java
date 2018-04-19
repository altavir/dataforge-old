/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.jfreechart;

import hep.dataforge.meta.Meta;
import hep.dataforge.plots.Plot;
import hep.dataforge.tables.Adapters;
import hep.dataforge.tables.ValuesAdapter;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.data.xy.AbstractIntervalXYDataset;

import java.util.List;

/**
 * Wrapper for plot. Multiple xs are not allowed
 *
 * @author Alexander Nozik
 */
final class JFCDataWrapper extends AbstractIntervalXYDataset {

    private Plot plot;

    private ValuesAdapter adapter;
    private List<Values> data;
    private Meta query = Meta.empty();
    private Integer index = 0;


    public JFCDataWrapper(Plot plot) {
        this.plot = plot;
        adapter = plot.getAdapter();
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Plot getPlot() {
        return plot;
    }

    public void setPlot(Plot plot) {
        if (this.plot != plot) {
            this.plot = plot;
            invalidateData();
        }
    }

    private synchronized List<Values> getData() {
        if (data == null) {
            data = plot.getData(query);
        }
        return data;
    }

    private synchronized Values getAt(int i) {
        return getData().get(i);
    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    @Override
    public Comparable<?> getSeriesKey(int i) {
        if (getSeriesCount() == 1) {
            return plot.getName().toUnescaped();
        } else {
            return plot.getName().append(Adapters.getTitle(adapter, Adapters.Y_AXIS)).toUnescaped();
        }
    }

    @NotNull
    @Override
    public Number getStartX(int i, int i1) {
        return Adapters.getLowerBound(adapter, Adapters.X_AXIS, getAt(i1));
    }

    @NotNull
    @Override
    public Number getEndX(int i, int i1) {
        return Adapters.getUpperBound(adapter, Adapters.X_AXIS, getAt(i1));
    }

    @NotNull
    @Override
    public Number getStartY(int i, int i1) {
        return Adapters.getLowerBound(adapter, Adapters.Y_AXIS, getAt(i1));
    }

    @NotNull
    @Override
    public Number getEndY(int i, int i1) {
        return Adapters.getUpperBound(adapter, Adapters.Y_AXIS, getAt(i1));
    }

    @Override
    public int getItemCount(int i) {
        return getData().size();
    }

    @Override
    public Number getX(int i, int i1) {
        return transform(Adapters.getXValue(adapter, getAt(i1)));
    }


    @Override
    public Number getY(int i, int i1) {
        return transform(Adapters.getYValue(adapter, getAt(i1)));
    }

    @Nullable
    private Number transform(Value value) {
        if (value.isNull()) {
            return null;
        } else {
            return value.getDouble();
        }
    }


    public void invalidateData() {
        this.data = null;
    }
}
