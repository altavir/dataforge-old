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

/**
 * <p>Abstract AbstractIntegrand class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class AbstractIntegrand implements Integrand{
    
    private double absoluteAccuracy = Double.POSITIVE_INFINITY;
    private double relativeAccuracy = Double.POSITIVE_INFINITY;
    private int iterations = 0;
    private int evaluations = 0;
    private Double value = Double.NaN;

    /**
     * <p>Constructor for AbstractIntegrand.</p>
     *
     * @param absoluteAccuracy a double.
     * @param relativeAccuracy a double.
     * @param iterations a int.
     * @param evaluations a int.
     * @param value a {@link java.lang.Double} object.
     */
    public AbstractIntegrand(double absoluteAccuracy, double relativeAccuracy, int iterations, int evaluations, Double value) {
        this.absoluteAccuracy = absoluteAccuracy;
        this.relativeAccuracy = relativeAccuracy;
        this.iterations = iterations;
        this.evaluations = evaluations;
        this.value = value;
    }

    /**
     * <p>Constructor for AbstractIntegrand.</p>
     */
    public AbstractIntegrand() {
    }

    /** {@inheritDoc} */
    @Override
    public double getAbsoluteAccuracy() {
        return absoluteAccuracy;
    }

    /** {@inheritDoc} */
    @Override
    public int getEvaluations() {
        return evaluations;
    }

    /** {@inheritDoc} */
    @Override
    public int getIterations() {
        return iterations;
    }

    /** {@inheritDoc} */
    @Override
    public double getRelativeAccuracy() {
        return relativeAccuracy;
    }

    /** {@inheritDoc} */
    @Override
    public Double getValue() {
        return value;
    }
    
}
