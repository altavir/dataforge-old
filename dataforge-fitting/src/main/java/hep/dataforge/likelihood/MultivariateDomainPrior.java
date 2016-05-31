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
package hep.dataforge.likelihood;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.functions.AbstractNamedFunction;
import hep.dataforge.maths.Domain;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;
import org.apache.commons.math3.exception.DimensionMismatchException;

/**
 * <p>MultivariateDomainPrior class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class MultivariateDomainPrior extends AbstractNamedFunction{
    
    private Domain dom;

    /**
     * <p>Constructor for MultivariateDomainPrior.</p>
     *
     * @param dom a {@link hep.dataforge.maths.Domain} object.
     * @param list an array of {@link java.lang.String} objects.
     */
    public MultivariateDomainPrior(Domain dom, String[] list) {
        super(list);
        if(dom.getDimension()!=list.length) {
            throw new DimensionMismatchException(dom.getDimension(), list.length);
        }
        this.dom = dom;
    }

    /**
     * <p>Constructor for MultivariateDomainPrior.</p>
     *
     * @param dom a {@link hep.dataforge.maths.Domain} object.
     * @param named a {@link hep.dataforge.names.Names} object.
     */
    public MultivariateDomainPrior(Domain dom, Names named) {
        super(named);
        if(dom.getDimension()!=named.getDimension()) {
            throw new DimensionMismatchException(dom.getDimension(), named.getDimension());
        }        
        this.dom = dom;
    }
    

    /** {@inheritDoc} */
    @Override
    public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException {
        if(!this.names().contains(derivParName)) {
            return 0;
        } else {
            throw new NotDefinedException();
        }
    }

        /** {@inheritDoc} */
    @Override
    public boolean providesDeriv(String name) {
        return !this.names().contains(name);
    }
        /** {@inheritDoc} */
        /** {@inheritDoc} */
    @Override
    public double value(NamedValueSet pars) {
        NamedVector vector = new NamedVector(pars);
        if(dom.contains(vector.getVector())) {
            return 1/dom.volume();
        } else {
            return 0;
        }
    }

    
}
