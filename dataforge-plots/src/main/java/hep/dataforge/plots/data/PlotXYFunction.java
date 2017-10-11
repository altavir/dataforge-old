/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.plots.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.tables.ValueMap;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.Values;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class for dynamic function values calculation for plot
 *
 * @author Alexander Nozik
 */
public class PlotXYFunction extends XYPlot {

    private static final int DEFAULT_DENSITY = 200;

    public static PlotXYFunction plotFunction(String name, Function<Double, Double> function, double from, double to, int numPoints) {
        PlotXYFunction p = new PlotXYFunction(name);
        p.setFunction(function);
        p.setXRange(from, to, false);
        p.setDensity(numPoints, false);
        return p;
    }

    private final NavigableMap<Double, Double> cache = new TreeMap<>();
    private final ObjectProperty<Function<Double, Double>> function = new SimpleObjectProperty<>();
    private final DoubleProperty lo = new SimpleDoubleProperty();
    private final DoubleProperty hi = new SimpleDoubleProperty();
    /**
     * The minimal number of points per range
     */
    private final IntegerProperty density = new SimpleIntegerProperty(DEFAULT_DENSITY);

    /**
     * @param name
     */
    public PlotXYFunction(String name) {
        super(name);
        getConfig().setValue("showLine", true);
        getConfig().setValue("showSymbol", false);
        function.addListener((ObservableValue<? extends Function<Double, Double>> observable,
                              Function<Double, Double> oldValue, Function<Double, Double> newValue) -> {
            invalidateCache();
        });
    }


    public void setFunction(Function<Double, Double> function) {
        this.function.set(function);
    }

    /**
     * Turns line smoothing on or off
     *
     * @param smoothing
     */
    public void setSmoothing(boolean smoothing) {
        if (smoothing) {
            getConfig().setValue("connectionType", "spline");
        } else {
            getConfig().setValue("connectionType", "default");
        }
    }

    /**
     * @param from   lower range boundary
     * @param to     upper range boundary
     * @param notify notify listeners
     */
    public void setXRange(double from, double to, boolean notify) {
        lo.set(from);
        hi.set(to);
        if (notify) {
            super.notifyDataChanged();
        }
    }

    /**
     * Set minimum number of nodes per range
     *
     * @param density
     * @param notify
     */
    public void setDensity(int density, boolean notify) {
        this.density.set(density);
        if (notify) {
            super.notifyDataChanged();
        }
    }

    /**
     * Split region into uniform blocks, then check if each block contains at
     * least one cached point and calculate additional point in the center of
     * the block if it does not.
     * <p>
     * If function is not set or desired density not positive does nothing.
     */
    protected void validateCache() {
        if (function.get() == null && density.get() > 0) {
            //do nothing if there is no function
            return;
        }
        // recalculate cache if boundaries are finite, otherwise use existing cache
        double from = lo.get();
        double to = hi.get();
        int nodes = this.density.get();
        if (Double.isFinite(from) && Double.isFinite(to)) {
            for (int i = 0; i < nodes; i++) {
                double blockBegin = from + i * (to - from) / (nodes - 1);
                double blockEnd = from + (i + 1) * (to - from) / (nodes - 1);
                if (cache.subMap(blockBegin, blockEnd).isEmpty()) {
                    eval((blockBegin + blockEnd) / 2);
                }
            }
        }
    }

    protected synchronized void invalidateCache() {
        this.cache.clear();
    }

    /**
     * Calculate function cache for the given point and return calculated value
     *
     * @param x
     */
    protected synchronized double eval(double x) {
        double y = function.get().apply(x);
        this.cache.put(x, y);
        return y;
    }

    /**
     * Give the fixed point in which this function must be calculated. Calculate value and update range if it does not include point
     *
     * @param x
     */
    public double calculateIn(double x) {
        if (this.lo.getValue() == null || this.lo.get() > x) {
            this.lo.set(x);
        }
        if (this.hi.getValue() == null || this.hi.get() < x) {
            this.hi.set(x);
        }
        return eval(x);
    }

    @Override
    protected List<Values> getRawData(Meta query) {
        //recalculate cache with default values
        if (query.hasValue("xRange.from")) {
            this.lo.set(query.getDouble("xRange.from"));
        }
        if (query.hasValue("xRange.to")) {
            this.hi.set(query.getDouble("xRange.to"));
        }
        if (query.hasValue("density")) {
            this.density.set(query.getInt("density"));
        }
        validateCache();
        return cache.entrySet().stream()
                .map(entry -> ValueMap.of(new String[]{XYAdapter.X_AXIS, XYAdapter.Y_AXIS}, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


}
