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

/**
 * <p>Spectrum interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Spectrum {

    /**
     * <p>value.</p>
     *
     * @param x a double.
     * @param pars an array of double.
     * @return a double.
     */
    double value(double x, double[] pars);

    /**
     * <p>derivValue.</p>
     *
     * @param i a int.
     * @param x a double.
     * @param pars an array of double.
     * @return a double.
     * @throws hep.dataforge.exceptions.NotDefinedException if any.
     */
    double derivValue(int i, double x, double[] pars) throws NotDefinedException;

    /**
     * <p>getDimension.</p>
     *
     * @return a int.
     */
    int getDimension();//метод для проверки совпадения размерностей

    /**
     * <p>providesDeriv.</p>
     *
     * @return a boolean.
     */
    boolean providesDeriv();
}
