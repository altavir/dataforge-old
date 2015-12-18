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
package hep.dataforge.functions;

import hep.dataforge.exceptions.NotDefinedException;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * <p>Function interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Function extends UnivariateFunction {
//    public double value(double x);

    /**
     * <p>derivValue.</p>
     *
     * @param x a double.
     * @return a double.
     * @throws hep.dataforge.exceptions.NotDefinedException if any.
     */
    double derivValue(double x) throws NotDefinedException;

    /**
     * <p>providesDeriv.</p>
     *
     * @return a boolean.
     */
    boolean providesDeriv();
}
