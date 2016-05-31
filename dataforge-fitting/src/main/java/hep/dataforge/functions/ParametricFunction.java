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

import hep.dataforge.names.NameSetContainer;
import hep.dataforge.values.NamedValueSet;

/**
 * <p>NamedSpectrum interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface ParametricFunction extends NameSetContainer {

    /**
     * <p>derivValue.</p>
     *
     * @param parName a {@link java.lang.String} object.
     * @param x a double.
     * @param set a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a double.
     */
    double derivValue(String parName, double x, NamedValueSet set);

    /**
     * <p>value.</p>
     *
     * @param x a double.
     * @param set a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a double.
     */
    double value(double x, NamedValueSet set);

    /**
     * <p>providesDeriv.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean providesDeriv(String name);
}
