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

/**
 * <p>Abstract Integrator class.</p>
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public abstract class Integrator<T extends Integrand> {

    /**
     * Integrate with default stopping condition for this integrator
     *
     * @param integrand a T object.
     * @return a T object.
     */
    public T evaluate(T integrand) {
        return Integrator.this.evaluate(integrand, getDefaultStopingCondition());
    }
    
    /**
     * Helper method for single integration
     *
     * @param integrand a T object.
     * @return a {@link java.lang.Double} object.
     */
    public Double integrate(T integrand){
        return evaluate(integrand).getValue();
    }
    
    /**
     * Integrate with supplied stopping condition
     *
     * @param integrand a T object.
     * @param condition a {@link java.util.function.Predicate} object.
     * @return a T object.
     */
    public abstract T evaluate(T integrand, Predicate<Integrand> condition);

    /**
     * Get default stopping condition for this integrator
     *
     * @return a {@link java.util.function.Predicate} object.
     */
    public abstract Predicate<Integrand> getDefaultStopingCondition();
}
