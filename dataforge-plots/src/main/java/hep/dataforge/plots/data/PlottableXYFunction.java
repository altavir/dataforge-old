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
import hep.dataforge.plots.XYPlottable;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.MapPoint;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class for dynamic function values calculation for plot
 *
 * @author Alexander Nozik
 */
public class PlottableXYFunction extends XYPlottable {

    private static final int DEFAULT_NODES_NUMBER = 200;
    private final NavigableMap<Double, Double> cache = new TreeMap<>();
    private final ObjectProperty<Function<Double, Double>> function = new SimpleObjectProperty();
    private final DoubleProperty lo = new SimpleDoubleProperty();
    private final DoubleProperty hi = new SimpleDoubleProperty();
    private final IntegerProperty density = new SimpleIntegerProperty(DEFAULT_NODES_NUMBER);
    /**
     *
     * @param name
     */
    public PlottableXYFunction(String name) {
        super(name);
        getConfig().setValue("showLine", true);
        getConfig().setValue("showSymbol", false);
        function.addListener((ObservableValue<? extends Function<Double, Double>> observable,
                Function<Double, Double> oldValue, Function<Double, Double> newValue) -> {
            invalidateCache();
        });
    }

    public static PlottableXYFunction plotFunction(String name, Function<Double, Double> function, double from, double to, int numPoints) {
        PlottableXYFunction p = new PlottableXYFunction(name);
        p.setFunction(function);
        p.setXRange(from, to, false);
        p.setDensity(numPoints, false);
        return p;
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
     *
     * @param from lower range boundary
     * @param to upper range boundary
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
     *
     * If function is not set or desired density not positive does nothing.
     *
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
    public Stream<DataPoint> dataStream(Meta cfg) {
        //recalculate cache with default values
        if (cfg.hasNode("xRange")) {
            this.lo.set(cfg.getDouble("xRange.from", lo.get()));
            this.hi.set(cfg.getDouble("xRange.to", hi.get()));
        }
        if (cfg.hasValue("density")) {
            this.density.set(cfg.getInt("density"));
        }
        validateCache();
        return filterDataStream(cache.entrySet().stream()
                .map(entry -> new MapPoint(new String[]{"x", "y"}, entry.getKey(), entry.getValue())), cfg);
    }

}
