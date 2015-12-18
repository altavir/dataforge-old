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
package hep.dataforge.maths;

import org.apache.commons.math3.distribution.AbstractMultivariateRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Генератор для вектора, равномерно распределенного в области определения
 * (домене)
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class MultivariateUniformDistribution extends AbstractMultivariateRealDistribution {

    private Domain domain;

    /**
     * <p>Constructor for MultivariateUniformDistribution.</p>
     *
     * @param rg a {@link org.apache.commons.math3.random.RandomGenerator} object.
     * @param loVals an array of double.
     * @param upVals an array of double.
     */
    public MultivariateUniformDistribution(RandomGenerator rg, double[] loVals, double[] upVals) {
        super(rg, loVals.length);
        if (loVals.length != upVals.length) {
            throw new IllegalArgumentException();
        }
        assert loVals.length == upVals.length;
        Double[] lower = new Double[loVals.length];
        Double[] upper = new Double[upVals.length];
        for (int i = 0; i < upper.length; i++) {
            lower[i] = loVals[i];
            upper[i] = upVals[i];
        }
        this.domain = new HyperSquareDomain(lower, upper);
    }

    /**
     * <p>Constructor for MultivariateUniformDistribution.</p>
     *
     * @param rg a {@link org.apache.commons.math3.random.RandomGenerator} object.
     * @param dom a {@link hep.dataforge.maths.Domain} object.
     */
    public MultivariateUniformDistribution(RandomGenerator rg, Domain dom) {
        super(rg, dom.getDimension());
        this.domain = dom;
    }

    /** {@inheritDoc}
     * @param doubles
     * @return  */
    @Override
    public double density(double[] doubles) {
        if (doubles.length != this.getDimension()) {
            throw new IllegalArgumentException();
        }
        if (!domain.contains(doubles)) {
            return 0;
        }
        return 1 / domain.volume();
    }

    /**
     * <p>getVolume.</p>
     *
     * @return a double.
     */
    public double getVolume() {
        return domain.volume();
    }

    /** {@inheritDoc}
     * @return  */
    @Override
    public double[] sample() {
        double[] res = new double[this.getDimension()];
        Double loval;
        Double upval;

        do {
            for (int i = 0; i < res.length; i++) {
                loval = domain.getLowerBound(i);
                upval = domain.getUpperBound(i);
                if (loval == upval) {
                    res[i] = loval;
                } else {
                    res[i] = loval + this.random.nextDouble() * (upval - loval);
                }

            }
        } while (!domain.contains(res));

        return res;
    }
}
