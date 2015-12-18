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
package hep.dataforge.maths.integration;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * <p>Sample class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Sample {

    private final double weight;
    private final RealVector sample;

    /**
     * <p>Constructor for Sample.</p>
     *
     * @param weight a double.
     * @param sample a {@link org.apache.commons.math3.linear.RealVector} object.
     */
    public Sample(double weight, RealVector sample) {
        this.weight = weight;
        this.sample = sample;
    }

    /**
     * <p>Constructor for Sample.</p>
     *
     * @param weight a double.
     * @param sample an array of double.
     */
    public Sample(double weight, double[] sample) {
        this.weight = weight;
        this.sample = new ArrayRealVector(sample);
    }

    /**
     * <p>getDimension.</p>
     *
     * @return a int.
     */
    public int getDimension() {
        return sample.getDimension();
    }

    /**
     * <p>Getter for the field <code>weight</code>.</p>
     *
     * @return a double.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * <p>getVector.</p>
     *
     * @return a {@link org.apache.commons.math3.linear.RealVector} object.
     */
    public RealVector getVector() {
        return sample;
    }

    /**
     * <p>getArray.</p>
     *
     * @return an array of double.
     */
    public double[] getArray() {
        return sample.toArray();
    }
}
