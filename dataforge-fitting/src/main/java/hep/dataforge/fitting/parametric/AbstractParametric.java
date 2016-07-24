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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.AbstractNamedSet;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;


public abstract class AbstractParametric extends AbstractNamedSet {


    public AbstractParametric(Names names) {
        super(names);
    }

    public AbstractParametric(String[] list) {
        super(list);
    }

    public AbstractParametric(NameSetContainer set) {
        super(set);
    }

    
    /**
     * Provide default value for parameter with name {@code name}. Throws
     * NameNotFound if no default found for given parameter.
     *
     * @param name
     * @return
     */
    protected double getDefaultParameter(String name) {
        throw new NameNotFoundException(name);
    }

    /**
     * Extract value from input vector using default value if required parameter
     * not found
     *
     * @param name
     * @param set
     * @return
     */
    protected double getParameter(String name, NamedValueSet set) {
        if (set.names().contains(name)) {
            return set.getDouble(name);
        } else {
            return getDefaultParameter(name);
        }
    }    
    
}
