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

import org.apache.commons.math3.analysis.MultivariateFunction;

/**
 * <p>MonteCarloIntegrand class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class MonteCarloIntegrand extends AbstractIntegrand{

    private final MultivariateFunction function;
    private final Sampler sampler;

//    private List<Double> previousResults;
    
    /**
     * <p>Constructor for MonteCarloIntegrand.</p>
     *
     * @param integrand a {@link hep.dataforge.maths.integration.MonteCarloIntegrand} object.
     * @param absoluteAccuracy a double.
     * @param relativeAccuracy a double.
     * @param iterations a int.
     * @param evaluations a int.
     * @param value a {@link java.lang.Double} object.
     */
    public MonteCarloIntegrand(MonteCarloIntegrand integrand, double absoluteAccuracy, double relativeAccuracy, int iterations, int evaluations, Double value) {
        super(absoluteAccuracy, relativeAccuracy, iterations, evaluations, value);
        this.function = integrand.getFunction();
        this.sampler = integrand.getSampler();
//        previousResults = new ArrayList<>();
//        previousResults.add(value);        
    }    
    
    /**
     * <p>Constructor for MonteCarloIntegrand.</p>
     *
     * @param function a {@link org.apache.commons.math3.analysis.MultivariateFunction} object.
     * @param sampler a {@link hep.dataforge.maths.integration.Sampler} object.
     * @param absoluteAccuracy a double.
     * @param relativeAccuracy a double.
     * @param iterations a int.
     * @param evaluations a int.
     * @param value a {@link java.lang.Double} object.
     */
    public MonteCarloIntegrand(MultivariateFunction function, Sampler sampler, double absoluteAccuracy, double relativeAccuracy, int iterations, int evaluations, Double value) {
        super(absoluteAccuracy, relativeAccuracy, iterations, evaluations, value);
        this.function = function;
        this.sampler = sampler;
//        previousResults = new ArrayList<>();
//        previousResults.add(value);
    }

    /**
     * <p>Constructor for MonteCarloIntegrand.</p>
     *
     * @param function a {@link org.apache.commons.math3.analysis.MultivariateFunction} object.
     * @param sampler a {@link hep.dataforge.maths.integration.Sampler} object.
     */
    public MonteCarloIntegrand(MultivariateFunction function, Sampler sampler) {
        super();
        this.function = function;
        this.sampler = sampler;
//        previousResults = new ArrayList<>();
    }
    
    
    
    
    /** {@inheritDoc}
     * @return  */
    @Override
    public int getDimension() {
        return getSampler().getDimension();
    }

    /**
     * <p>getFunctionValue.</p>
     *
     * @param x an array of double.
     * @return a double.
     */
    public double getFunctionValue(double[] x){
        return function.value(x);
    }

//    /**
//     * @return the previousResults
//     */
//    public List<Double> getPreviousResults() {
//        return previousResults;
//    }
//    
    /**
     * <p>getSample.</p>
     *
     * @return a {@link hep.dataforge.maths.integration.Sample} object.
     */
    public Sample getSample(){
        return sampler.nextSample();
    }
    
    /**
     * <p>Getter for the field <code>function</code>.</p>
     *
     * @return the function
     */
    public MultivariateFunction getFunction() {
        return function;
    }

    /**
     * <p>Getter for the field <code>sampler</code>.</p>
     *
     * @return the sampler
     */
    public Sampler getSampler() {
        return sampler;
    }
    
}
