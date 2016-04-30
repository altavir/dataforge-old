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
package hep.dataforge.datafitter.models;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.names.Names;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.values.Value;
import static java.lang.Math.sqrt;

/**
 * <p>
 * HistogramBin class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class HistogramBin implements DataPoint {

    private static final String[] names = {"binBegin", "binEnd", "count", "binCenter", "binSize"};
    private double binBegin;
    private double binEnd;
    private long count;
    private final Names parNames;

    /**
     * <p>
     * Constructor for HistogramBin.</p>
     *
     * @param source a {@link hep.dataforge.datafitter.models.HistogramBin}
     * object.
     */
    protected HistogramBin(HistogramBin source) {
        parNames = source.names();
        this.binBegin = source.binBegin;
        this.binEnd = source.binEnd;
        this.count = source.count;
    }

    /**
     * <p>
     * Constructor for HistogramBin.</p>
     *
     * @param binBegin a double.
     * @param binEnd a double.
     * @param count a long.
     */
    public HistogramBin(double binBegin, double binEnd, long count) {
        parNames = Names.of(names);
        if (binBegin >= binEnd) {
            throw new IllegalArgumentException("Incorrect bin boundaries.");
        }
        if (count < 0) {
            throw new IllegalArgumentException("Count number is negative.");
        }
        this.binBegin = binBegin;
        this.binEnd = binEnd;
        this.count = count;
    }

    /**
     * <p>
     * binBegin.</p>
     *
     * @return a double.
     */
    public double binBegin() {
        return binBegin;
    }

    /**
     * <p>
     * binCenter.</p>
     *
     * @return a double.
     */
    public double binCenter() {
        return (binBegin + binEnd) / 2;
    }

    /**
     * <p>
     * binEnd.</p>
     *
     * @return a double.
     */
    public double binEnd() {
        return binEnd;
    }

    /**
     * <p>
     * binSize.</p>
     *
     * @return a double.
     */
    public double binSize() {
        //Предполагаем возможность неравномерных бинов
        return (binEnd - binBegin);
    }

    /**
     * <p>
     * count.</p>
     *
     * @return a long.
     */
    public long count() {
        return count;
    }

    /**
     * <p>
     * countErr.</p>
     *
     * @return a double.
     */
    public double countErr() {
        return sqrt(count);
    }

    /**
     * {@inheritDoc}
     *
     * @param name
     * @return
     */
    @Override
    public Value getValue(String name) {
        switch (name) {
            case "binBegin":
                return Value.of(binBegin);
            case "binEnd":
                return Value.of(binEnd);
            case "count":
                return Value.of(count);
            case "binCenter":
                return Value.of(binCenter());
            case "binSize":
                return Value.of(binSize());
            default:
                throw new NotDefinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasValue(String path) {
        return names().contains(path);
        /**
         * {@inheritDoc}
         */
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Names names() {
        return parNames;
    }
}
