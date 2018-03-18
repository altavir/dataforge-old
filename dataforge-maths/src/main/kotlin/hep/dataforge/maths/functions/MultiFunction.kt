/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.maths.functions

import hep.dataforge.exceptions.NotDefinedException
import org.apache.commons.math3.analysis.MultivariateFunction

/**
 *
 *
 * MultiFunction interface.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
interface MultiFunction : MultivariateFunction {

    val dimension: Int//метод для проверки совпадения размерностей

    @Throws(NotDefinedException::class)
    fun derivValue(n: Int, pars: DoubleArray): Double

    /**
     * Позволяет узнать, выдает ли функция аналитическую производную. Допускает
     * аргумент -1, в этом случае возвращает true, если заведомо есть
     * производные по всем параметрам. Возможна ситуация, когда providesDeriv
     * возвращает false в то время, как derivValue не выкидывает
     * NotDefinedException. В этом случае производная возвращается, но
     * пользоваться ей не рекомендуется.
     *
     * @param n a int.
     * @return a boolean.
     */
    fun providesDeriv(n: Int): Boolean
}
