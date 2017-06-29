package hep.dataforge.maths.histogram;

import hep.dataforge.maths.Domain;

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
     * Set the counter and return old value
     *
     * @param c
     * @return
     */
    long setCounter(long c);

    long getBinID();
}
