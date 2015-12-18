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

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.names.NamedSet;

/**
 * <p>NamedFunction interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface NamedFunction extends NamedSet {

    /**
     * <p>value.</p>
     *
     * @param pars a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a double.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    double value(NamedDoubleSet pars) throws NamingException;

    /**
     * <p>derivValue.</p>
     *
     * @param derivParName a {@link java.lang.String} object.
     * @param pars a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return 0 если имени нет в списке.
     * @throws hep.dataforge.exceptions.NotDefinedException - выкидывается только если имя есть в списке имен, но аналитической производной нет
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    double derivValue(String derivParName, NamedDoubleSet pars) throws NotDefinedException,NamingException;

    /**
     * Возвращает true только если производная явно задана. Если имени нет в списке параметров, должно возврашать false
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean providesDeriv(String name);
}
