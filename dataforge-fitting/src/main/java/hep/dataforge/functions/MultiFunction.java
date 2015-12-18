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
import org.apache.commons.math3.analysis.MultivariateFunction;

/**
 * <p>MultiFunction interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface MultiFunction extends MultivariateFunction {

    //   public double value(double[] pars);
    /**
     * <p>derivValue.</p>
     *
     * @param n a int.
     * @param pars an array of double.
     * @return a double.
     * @throws hep.dataforge.exceptions.NotDefinedException if any.
     */
    double derivValue(int n, double[] pars) throws NotDefinedException;

    /**
     * <p>getDimension.</p>
     *
     * @return a int.
     */
    int getDimension();//метод для проверки совпадения размерностей

    /**
     * Позволяет узнать, выдает ли функция аналитическую производную.
     * Допускает аргумент -1, в этом случае возвращает true, если заведомо есть производные по всем параметрам.
     * Возможна ситуация, когда providesDeriv возвращает false в то время, как derivValue не выкидывает NotDefinedException.
     * В этом случае производная возвращается, но пользоваться ей не рекомендуется.
     *
     * @param n a int.
     * @return a boolean.
     */
    boolean providesDeriv(int n);
}
