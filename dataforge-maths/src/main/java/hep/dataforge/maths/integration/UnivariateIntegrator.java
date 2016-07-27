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
public abstract class UnivariateIntegrator<T extends UnivariateIntegrand> implements Integrator<T> {

    /**
     * Create initial Integrand for given function and borders. This method is
     * required to initialize any
     *
     * @param function a
     * {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     * @return a T object.
     */
    protected abstract T init(UnivariateFunction function, Double lower, Double upper);

    public T evaluate(UnivariateFunction function, Double lower, Double upper) {
        return evaluate(UnivariateIntegrator.this.init(function, lower, upper));
    }

    public Double integrate(UnivariateFunction function, Double lower, Double upper) {
        return evaluate(function, lower, upper).getValue();
    }

    public T evaluate(Predicate<T> condition, UnivariateFunction function, Double lower, Double upper) {
        return evaluate(init(function, lower, upper), condition);
    }

    public T init(UnivariateIntegrand integrand) {
        return evaluate(integrand.getFunction(), integrand.getLower(), integrand.getUpper());
    }

    public T init(UnivariateIntegrand integrand, Predicate<T> condition) {
        return evaluate(condition, integrand.getFunction(), integrand.getLower(), integrand.getUpper());
    }
}
