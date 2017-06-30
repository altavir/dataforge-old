package hep.dataforge.maths.histogram;

import hep.dataforge.maths.HyperSquareDomain;
import hep.dataforge.tables.ValueMap;
import hep.dataforge.values.Values;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by darksnake on 29-Jun-17.
 */
public class SquareBin extends HyperSquareDomain implements Bin {

    private final long binId;
    private AtomicLong counter = new AtomicLong(0);

    /**
     * Create a multivariate bin
     *
     * @param lower
     * @param upper
     */
    public SquareBin(long binId, Double[] lower, Double[] upper) {
        super(lower, upper);
        this.binId = binId;
    }

    /**
     * Create a univariate bin
     *
     * @param lower
     * @param upper
     */
    public SquareBin(long binId, Double lower, Double upper) {
        super(new Double[]{lower}, new Double[]{upper});
        this.binId = binId;
    }

    /**
     * Get the lower bound for 0 axis
     *
     * @return
     */
    public Double getLowerBound() {
        return this.getLowerBound(0);
    }

    /**
     * Get the upper bound for 0 axis
     *
     * @return
     */
    public Double getUpperBound() {
        return this.getUpperBound(0);
    }

    @Override
    public long inc() {
        return counter.incrementAndGet();
    }

    @Override
    public long getCount() {
        return counter.get();
    }

    @Override
    public long setCounter(long c) {
        return counter.getAndSet(c);
    }

    @Override
    public long getBinID() {
        return binId;
    }

    @Override
    public Values describe(String... override) {
        ValueMap.Builder builder = new ValueMap.Builder();
        for (int i = 0; i < getDimension(); i++) {
            String axisName = getAxisName(i, override);
            Double binStart = getLowerBound(i);
            Double binEnd = getUpperBound(i);
            builder.putValue(axisName, binStart);
            builder.putValue(axisName + ".binEnd", binEnd);
        }
        builder.putValue("count.value", getCount());
        builder.putValue("id", getBinID());
        return builder.build();
    }

    protected String getAxisName(int i, String... override) {
        if (i < override.length) {
            return override[i];
        } else if (getDimension() <= 3) {
            switch (i) {
                case 0:
                    return "x";
                case 1:
                    return "y";
                case 2:
                    return "z";
                default:
                    throw new Error("Unreachable statement");
            }
        } else {
            return "axis_" + i;
        }
    }
}