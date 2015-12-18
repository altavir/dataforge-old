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

import java.util.function.Predicate;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * General ancestor for univariate integrators
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public abstract class UnivariateIntegrator<T extends UnivariateIntegrand> extends Integrator<T> {
    
    /**
     * Create initial Integrand for given function and borders. This method is required to initialize any
     *
     * @param function a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     * @return a T object.
     */
    protected abstract T init(UnivariateFunction function, Double lower, Double upper);
    
    /**
     * <p>evaluate.</p>
     *
     * @param function a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     * @return a T object.
     */
    public T evaluate(UnivariateFunction function, Double lower, Double upper){
        return evaluate(UnivariateIntegrator.this.init(function, lower, upper));
    }
    
    /**
     * <p>integrate.</p>
     *
     * @param function a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     * @return a {@link java.lang.Double} object.
     */
    public Double integrate(UnivariateFunction function, Double lower, Double upper){
        return evaluate(function, lower, upper).getValue();
    }
    
    /**
     * <p>evaluate.</p>
     *
     * @param condition a {@link java.util.function.Predicate} object.
     * @param function a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     * @return a T object.
     */
    public T evaluate(Predicate<Integrand> condition, UnivariateFunction function, Double lower, Double upper){
        return evaluate(UnivariateIntegrator.this.init(function, lower, upper),condition);
    }    
    
    /**
     * <p>init.</p>
     *
     * @param integrand a {@link hep.dataforge.maths.integration.UnivariateIntegrand} object.
     * @return a T object.
     */
    public T init(UnivariateIntegrand integrand){
        return UnivariateIntegrator.this.evaluate(integrand.getFunction(), integrand.getLower(), integrand.getUpper());
    }
    
    /**
     * <p>init.</p>
     *
     * @param integrand a {@link hep.dataforge.maths.integration.UnivariateIntegrand} object.
     * @param condition a {@link java.util.function.Predicate} object.
     * @return a T object.
     */
    public T init(UnivariateIntegrand integrand, Predicate<Integrand> condition){
        return evaluate(condition, integrand.getFunction(), integrand.getLower(), integrand.getUpper());
    }    
}
