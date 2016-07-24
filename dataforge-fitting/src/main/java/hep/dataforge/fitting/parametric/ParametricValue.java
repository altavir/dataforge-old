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
package hep.dataforge.fitting.parametric;

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.values.NamedValueSet;

/**
 * A value calculated from a set of parameters
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface ParametricValue extends NameSetContainer {

    double value(NamedValueSet pars) throws NamingException;

    double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException,NamingException;

    /**
     * Возвращает true только если производная явно задана. Если имени нет в списке параметров, должно возврашать false
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean providesDeriv(String name);
}
