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
package hep.dataforge.plots.fx;

import hep.dataforge.utils.DateTimeUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.chart.Axis;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspired by DateAxis implementation by hansolo
 * (https://bitbucket.org/hansolo/dateaxis310)
 *
 * @author Alexander Nozik
 * @param <T>
 */
public abstract class FXObjectAxis<T> extends Axis<T> {

    private final ObjectProperty<Range<T>> currentRangeProperty;
    private final Range<T> defaultRange;
    private Range<T> dataRange;

    //TODO add maxElementCount and maxAge properties
    //TODO add advanced tick label formatter
//    private ChartLayoutAnimator animator;
//    private Object currentAnimationID;
    public FXObjectAxis(T lower, T upper) {
        this.defaultRange = new Range<>(lower, upper);

        currentRangeProperty = new SimpleObjectProperty<>(this, "currentRange", defaultRange);
    }

    @Override
    protected Object autoRange(double length) {
        if (this.dataRange == null) {
            return defaultRange;
        } else {
            return this.dataRange;
        }
    }

    @Override
    public void invalidateRange(List<T> data) {
        super.invalidateRange(data);
        if (data.isEmpty()) {
            dataRange = defaultRange;
        } else if (data.size() == 1) {
            dataRange = joinRanges(defaultRange, new Range<>(data.get(0), data.get(0)));
        } else {
            dataRange = new Range<>(data.get(0), data.get(data.size() - 1));
        }
    }

    @Override
    protected void setRange(Object range, boolean animate) {
        Range<T> theRange = (Range<T>) range;
        this.currentRangeProperty.set(theRange);
        //TODO add animation evaluation here
    }

    @Override
    protected Object getRange() {
        return currentRangeProperty.get();
    }

    @Override
    public double getZeroPosition() {
        return 0;
    }

    private double getAxisLength() {
        return getSide().isHorizontal() ? getWidth() : getHeight();
    }

    @Override
    public double getDisplayPosition(T value) {
        Range<T> currentRange = currentRangeProperty.get();

        double axisLength = getAxisLength();

        double rangeSize = getDistance(currentRange.getLower(), currentRange.getUpper());

        //relative distance from range start to value multiplied by axis length
        if (getSide().isVertical()) {
            return (1 - getDistance(currentRange.getLower(), value) / rangeSize) * axisLength;
        } else {
            return getDistance(currentRange.getLower(), value) / rangeSize * axisLength;
        }
    }

    @Override
    public T getValueForDisplay(double displayPosition) {
        Range<T> currentRange = currentRangeProperty.get();

        double axisLength = getAxisLength();

        double rangeSize = getDistance(currentRange.getLower(), currentRange.getUpper());
        if (getSide().isVertical()) {
            return toRealValue((1 - displayPosition / axisLength) * rangeSize + toNumericValue(currentRange.getLower()));
        } else {
            return toRealValue(displayPosition / axisLength * rangeSize + toNumericValue(currentRange.getLower()));
        }

    }

    @Override
    public boolean isValueOnAxis(T value) {
        Range<T> currentRange = currentRangeProperty.get();
        double numeric = toNumericValue(value);
        return numeric > toNumericValue(currentRange.getLower()) && numeric < toNumericValue(currentRange.getUpper());
    }

    @Override
    protected List<T> calculateTickValues(double length, Object range) {
        Range<T> theRange = (Range<T>) range;

        int numLabels = Math.max((int) (length / measureTickMarkLabelSize(DateTimeUtils.now().toString(), getTickLabelRotation()).getWidth()) - 1, 1);

        double fromNumber = toNumericValue(theRange.getLower());
        double toNumber = toNumericValue(theRange.getUpper());

        double step = (toNumber - fromNumber) / numLabels;

        List<T> res = new ArrayList<>();
        res.add(theRange.getLower());
        double currentTick = fromNumber + step;
        while (currentTick < toNumber) {
            res.add(toRealValue(currentTick));
            currentTick += step;
        }
        res.add(theRange.getUpper());
        return res;
    }

    protected Range<T> joinRanges(Range<T> first, Range<T> second) {
        double min = Math.min(toNumericValue(first.getLower()), toNumericValue(second.getLower()));
        double max = Math.max(toNumericValue(first.getUpper()), toNumericValue(second.getUpper()));
        return new Range<>(toRealValue(min), toRealValue(max));
    }

    @Override
    protected abstract String getTickMarkLabel(T value);

    protected double getDistance(T from, T to) {
        double res = toNumericValue(to) - toNumericValue(from);
//        if (res < 0) {
//            throw new IllegalArgumentException("Wrong order of distance parameters");
//        }
        return res;
    }

    @Override
    public abstract double toNumericValue(T value);

    @Override
    public abstract T toRealValue(double value);

    protected class Range<T> {

        private T lower;
        private T upper;

        public Range(T lower, T upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public T getUpper() {
            return upper;
        }

        public T getLower() {
            return lower;
        }
    }
}
