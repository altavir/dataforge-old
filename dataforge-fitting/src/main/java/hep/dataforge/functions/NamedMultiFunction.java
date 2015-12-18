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
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.names.Names;

/**
 * Универсальная обертка, которая объединяет именованную и обычную функцию.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class NamedMultiFunction implements NamedFunction, MultiFunction {

    private final MultiFunction multiFunc;
    private final NamedFunction nFunc;
    private final Names names;

    /**
     * <p>Constructor for NamedMultiFunction.</p>
     *
     * @param names a {@link hep.dataforge.names.Names} object.
     * @param multiFunc a {@link hep.dataforge.functions.MultiFunction} object.
     */
    public NamedMultiFunction(Names names, MultiFunction multiFunc) {
        this.names = names;
        this.nFunc = null;
        this.multiFunc = multiFunc;
    }

    /**
     * <p>Constructor for NamedMultiFunction.</p>
     *
     * @param nFunc a {@link hep.dataforge.functions.NamedFunction} object.
     */
    public NamedMultiFunction(NamedFunction nFunc) {
        this.names = nFunc.names();
        this.nFunc = nFunc;
        this.multiFunc = null;
    }

    /** {@inheritDoc} */
    @Override
    public double derivValue(String parName, NamedDoubleSet pars) {
        if (nFunc != null) {
            return nFunc.derivValue(parName, pars);
        } else {
            if (!pars.names().contains(names.asArray())) {
                throw new IllegalArgumentException("Wrong parameter set.");
            }
            if (!names.contains(parName)) {
                throw new IllegalArgumentException("Wrong derivative parameter name.");
            }
            return this.multiFunc.derivValue(this.getNumberByName(parName), pars.getValues(this.names().asArray()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public double derivValue(int n, double[] vector) throws NotDefinedException {
        if (multiFunc != null) {
            return multiFunc.derivValue(n, vector);
        } else {
            NamedDoubleArray set = new NamedDoubleArray(names.asArray(), vector);
            return nFunc.derivValue(names.asArray()[n], set);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /** {@inheritDoc} */
    @Override
    public Names names() {
        return names;
    }

    private int getNumberByName(String name) {
        return this.names().asList().indexOf(name);
    }

    /**
     * {@inheritDoc}
     *
     * Подразумевается, что аналитически заданы все(!) производные
     */
    @Override
    public boolean providesDeriv(int n) {
        if (nFunc != null && nFunc.providesDeriv(this.names().asArray()[n])) {
            return true;
        }
        return multiFunc != null && multiFunc.providesDeriv(n);
    }

    /** {@inheritDoc} */
    @Override
    public boolean providesDeriv(String name) {
        if (nFunc != null) {
            return nFunc.providesDeriv(name);
        } else {
            return multiFunc.providesDeriv(this.getNumberByName(name));
        }
    }

    
    /** {@inheritDoc} */
    @Override
    public double value(NamedDoubleSet pars) {
        if (nFunc != null) {
            return nFunc.value(pars);
        } else {
            if (!pars.names().contains(names.asArray())) {
                throw new IllegalArgumentException("Wrong parameter set.");
            }
            return this.value(pars.getValues(this.names().asArray()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public double value(double[] vector){
        if (multiFunc != null) {
            return multiFunc.value(vector);
        } else {
            NamedDoubleArray set = new NamedDoubleArray(names.asArray(), vector);
            return nFunc.value(set);
        }
    }
}
