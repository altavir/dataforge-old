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

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * <p>UnivariateIntegrand class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class UnivariateIntegrand extends AbstractIntegrand {

    private final UnivariateFunction function;
    /*
     In theory it is possible to make infinite bounds
     */
    private Double lower;
    private Double upper;

    /**
     * <p>Constructor for UnivariateIntegrand.</p>
     *
     * @param function a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     */
    public UnivariateIntegrand(UnivariateFunction function, Double lower, Double upper) {
        super();
        this.function = function;
        if (lower >= upper) {
            throw new IllegalArgumentException("Wrong bounds for integrand");
        }
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * <p>Constructor for UnivariateIntegrand.</p>
     *
     * @param integrand a {@link hep.dataforge.maths.integration.UnivariateIntegrand} object.
     * @param absoluteAccuracy a double.
     * @param relativeAccuracy a double.
     * @param iterations a int.
     * @param evaluations a int.
     * @param value a {@link java.lang.Double} object.
     */
    public UnivariateIntegrand(UnivariateIntegrand integrand, double absoluteAccuracy, double relativeAccuracy, int iterations, int evaluations, Double value) {
        super(absoluteAccuracy, relativeAccuracy, iterations, evaluations, value);
        this.function = integrand.getFunction();
        if (integrand.getLower() >= integrand.getUpper()) {
            throw new IllegalArgumentException("Wrong bounds for integrand");
        }
        this.lower = integrand.getLower();
        this.upper = integrand.getUpper();         
    }

    /** {@inheritDoc}
     * @return  */
    @Override
    public int getDimension() {
        return 1;
    }

    /**
     * <p>getFunctionValue.</p>
     *
     * @param x a double.
     * @return a double.
     */
    public double getFunctionValue(double x) {
        return function.value(x);
    }

    /**
     * <p>Getter for the field <code>function</code>.</p>
     *
     * @return the function
     */
    public UnivariateFunction getFunction() {
        return function;
    }

    /**
     * <p>Getter for the field <code>lower</code>.</p>
     *
     * @return the lower
     */
    public Double getLower() {
        return lower;
    }

    /**
     * <p>Getter for the field <code>upper</code>.</p>
     *
     * @return the upper
     */
    public Double getUpper() {
        return upper;
    }

}
