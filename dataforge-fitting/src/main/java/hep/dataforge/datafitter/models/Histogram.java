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

import hep.dataforge.points.ListPointSet;
import hep.dataforge.exceptions.NamingException;
import java.util.Arrays;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;

/**
 * <p>
 * Histogram class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Histogram extends ListPointSet {

    public static String[] names = {"binBegin", "binEnd", "count"};
    private double[] borders;

    /**
     * Create an empty histogram with uniformly distributed bin borders
     *
     * @param name
     * @param begin a double.
     * @param end a double.
     * @param binSize a double.
     */
    public Histogram(double begin, double end, double binSize) {
        super(names);
        this.borders = getUniformUnivariateGrid(begin, end, binSize);
    }

    /**
     * Create an empty histogram with custom borders
     *
     * @param borders
     */
    public Histogram(double[] borders) {
        super(names);
        this.borders = borders;
        Arrays.sort(borders);
    }

    /**
     * Create an empty histogram with uniformly distributed bin borders
     *
     * @param name
     * @param begin a double.
     * @param binSize a double.
     * @param binNum a int.
     */
    public Histogram(double begin, double binSize, int binNum) {
        super(names);
        this.borders = getUniformUnivariateGrid(begin, binSize * binNum, binNum);
    }

    private int countInBin(Number[] data, double a, double b) {
        assert a < b;

        int counter = 0;
        for (Number n : data) {
            if ((n.doubleValue() >= a) && (n.doubleValue() <= b)) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * convert data to Number[] ignoring NaN values and fill
     *
     * @param data
     */
    public void fill(double... data) {
        Number[] objectData = new Number[data.length];
        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i])) {
                objectData[i] = data[i];
            }
        }
        fill(objectData);
    }

    /**
     * Fill the histogram clearing all old data
     *
     * @param data
     */
    public void fill(Number... data) {
        if (borders.length < 2) {
            throw new IllegalStateException("Borders array length is too small");
        }

        for (int i = 0; i < borders.length - 1; i++) {
            try {
                this.add(new HistogramBin(borders[i], borders[i + 1], countInBin(data, borders[i], borders[i + 1])));
            } catch (NamingException ex) {
                throw new Error(ex);
            }
        }
    }

}
