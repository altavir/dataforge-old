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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.names.AbstractNamedSet;
import hep.dataforge.names.Names;
import hep.dataforge.names.NameSetContainer;

/**
 * <p>
 * Abstract AbstractNamedSpectrum class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class AbstractParametricFunction extends AbstractNamedSet implements ParametricFunction {

    /**
     * <p>
     * Constructor for AbstractNamedSpectrum.</p>
     *
     * @param names a {@link hep.dataforge.names.Names} object.
     */
    public AbstractParametricFunction(Names names) {
        super(names);
    }

    /**
     * <p>
     * Constructor for AbstractNamedSpectrum.</p>
     *
     * @param list an array of {@link java.lang.String} objects.
     */
    public AbstractParametricFunction(String[] list) {
        super(list);
    }

    /**
     * <p>
     * Constructor for AbstractNamedSpectrum.</p>
     *
     * @param set a {@link hep.dataforge.names.NameSetContainer} object.
     */
    public AbstractParametricFunction(NameSetContainer set) {
        super(set);
    }

    /**
     * Provide default value for parameter with name {@code name}. Throws
     * NameNotFound if no default found for given parameter.
     *
     * @param name
     * @return
     */
    protected double getDefaultValue(String name){
        throw new NameNotFoundException(name);
    }
    
    /**
     * Extract value from input vector using default value if required parameter not found
     * @param name
     * @param set
     * @return 
     */
    protected double getValue(String name, NamedDoubleSet set){
        if(set.names().contains(name)){
            return set.getValue(name);
        } else {
            return getDefaultValue(name);
        }
    }

}
