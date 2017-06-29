package hep.dataforge.maths.histogram;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A thread safe histogram
 * Created by darksnake on 29-Jun-17.
 */
public abstract class Histogram implements BinFactory {

    /**
     * Lookup a bin containing specific point if it is present
     *
     * @param point
     * @return
     */
    public abstract Optional<Bin> lookupBin(Double... point);

    /**
     * Add a bin to storage
     *
     * @param bin
     * @return
     */
    protected abstract Bin addBin(Bin bin);

    /**
     * Find or create a bin containing given point and return number of counts in bin after addition
     *
     * @param point
     * @return
     */
    public long put(Double... point) {
        Bin bin = lookupBin(point).orElseGet(() -> addBin(getBin(point)));
        //PENDING add ability to do some statistical analysis on flight?
        return bin.inc();
    }

    public void putAll(Stream<Double[]> stream) {
        stream.parallel().forEach(this::put);
    }

    public abstract Bin getBinById(long id);

//    public Table asTable() {
//
//    }
}

