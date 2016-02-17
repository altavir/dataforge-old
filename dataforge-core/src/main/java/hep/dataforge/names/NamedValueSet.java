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
package hep.dataforge.names;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.values.Value;

/**
 * <p>
 * NamedValueSet interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface NamedValueSet extends NamedSet, Cloneable {

    /**
     * <p>
     * getValue.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.values.Value} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    Value getValue(String name) throws NameNotFoundException;

}
