package hep.dataforge.maths.histogram;

import hep.dataforge.maths.GridCalculator;
import hep.dataforge.tables.TableFormat;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.DoubleStream;

/**
 * A univariate histogram with fast bin lookup.
 * Created by darksnake on 02.07.2017.
 */
public class UnivariateHistogram extends Histogram {
    private final double[] borders;
    private TreeMap<Double, Bin> binMap = new TreeMap<>();

    public UnivariateHistogram(double[] borders) {
        this.borders = borders;
        Arrays.sort(borders);
    }

    public UnivariateHistogram(Double start, Double stop, Double step) {
        this.borders =
                GridCalculator.getUniformUnivariateGrid(start, stop, step);
    }


    private Double getValue(Double... point) {
        if (point.length != 1) {
            throw new DimensionMismatchException(point.length, 1);
        } else {
            return point[0];
        }
    }

    @Override
    public Bin createBin(Double... point) {
        Double value = getValue(point);
        int index = -Arrays.binarySearch(borders, value);
        if (index > 0) {
            return new SquareBin(Double.NEGATIVE_INFINITY, borders[0]);
        } else if (index >= borders.length) {
            return new SquareBin(borders[borders.length - 1], Double.POSITIVE_INFINITY);
        } else {
            return new SquareBin(borders[index], borders[index + 1]);
        }
    }

    @Override
    public Optional<Bin> findBin(Double... point) {
        Double value = getValue(point);
        Map.Entry<Double, Bin> entry = binMap.floorEntry(value);
        if (entry != null && entry.getValue().contains(point)) {
            return Optional.of(entry.getValue());
        } else {
            return Optional.empty();
        }

    }

    @Override
    protected Bin addBin(Bin bin) {
        //The call should be thread safe. New bin is added only if it is absent
        return binMap.computeIfAbsent(bin.getLowerBound(0), (id) -> bin);
    }

    @Override
    protected TableFormat getFormat() {
        return null;
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @NotNull
    @Override
    public Iterator<Bin> iterator() {
        return binMap.values().iterator();
    }

    public void fill(DoubleStream stream){
        stream.parallel().forEach(this::put);
    }
}
