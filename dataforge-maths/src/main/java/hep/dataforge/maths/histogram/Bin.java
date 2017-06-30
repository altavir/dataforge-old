package hep.dataforge.maths.histogram;

import hep.dataforge.maths.Domain;
import hep.dataforge.values.Values;

/**
 * Created by darksnake on 29-Jun-17.
 */
public interface Bin extends Domain {

    /**
     * Increment counter and return new value
     *
     * @return
     */
    long inc();

    /**
     * The number of counts in bin
     *
     * @return
     */
    long getCount();

    /**
     * Set the counter and return old value
     *
     * @param c
     * @return
     */
    long setCounter(long c);

    long getBinID();

    /**
     * Get the description of this bin as a set of named values
     *
     * @param namesOverride The names to be used for axis names if not present,then using default
     * @return
     */
    Values describe(String... namesOverride);
}
