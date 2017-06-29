package hep.dataforge.maths.histogram;

import hep.dataforge.maths.HyperSquareDomain;

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
    public long setCounter(long c) {
        return counter.getAndSet(c);
    }

    @Override
    public long getBinID() {
        return binId;
    }
}