package hep.dataforge.maths.histogram;

import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.TableFormat;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A thread safe histogram
 * Created by darksnake on 29-Jun-17.
 */
public abstract class Histogram implements BinFactory, Iterable<Bin> {

    /**
     * Lookup a bin containing specific point if it is present
     *
     * @param point
     * @return
     */
    public abstract Optional<Bin> findBin(Double... point);

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
        Bin bin = findBin(point).orElseGet(() -> addBin(createBin(point)));
        //PENDING add ability to do some statistical analysis on flight?
        return bin.inc();
    }

    public void putAll(Stream<Double[]> stream) {
        stream.parallel().forEach(this::put);
    }

    public void putAll(Iterable<Double[]> iter) {
        iter.forEach(this::put);
    }

    public abstract Bin getBinById(long id);

    public Stream<Bin> binStream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Construct a format for table using given names as axis names. The number of input names should equal to the
     * dimension of this histogram or exceed it by one. In later case the last name is count axis name.
     *
     * @param names
     * @return
     */
    protected abstract TableFormat getFormat(String... names);

    /**
     * @return
     */
    public Table asTable(String... names) {
        return new ListTable(getFormat(names), binStream().map(bin -> bin.describe(names)));
    }
}

