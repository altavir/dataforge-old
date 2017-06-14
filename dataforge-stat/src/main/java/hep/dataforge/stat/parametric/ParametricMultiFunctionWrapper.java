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
package hep.dataforge.stat.parametric;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.maths.MathUtils;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.maths.functions.MultiFunction;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;

/**
 * Универсальная обертка, которая объединяет именованную и обычную функцию.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ParametricMultiFunctionWrapper implements ParametricValue, MultiFunction {

    private final MultiFunction multiFunc;
    private final ParametricValue nFunc;
    private final Names names;

    public ParametricMultiFunctionWrapper(Names names, MultiFunction multiFunc) {
        this.names = names;
        this.nFunc = null;
        this.multiFunc = multiFunc;
    }

    public ParametricMultiFunctionWrapper(ParametricValue nFunc) {
        this.names = nFunc.names();
        this.nFunc = nFunc;
        this.multiFunc = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double derivValue(String parName, NamedValueSet pars) {
        if (nFunc != null) {
            return nFunc.derivValue(parName, pars);
        } else {
            if (!pars.names().contains(names.asArray())) {
                throw new IllegalArgumentException("Wrong parameter set.");
            }
            if (!names.contains(parName)) {
                throw new IllegalArgumentException("Wrong derivative parameter name.");
            }
            return this.multiFunc.derivValue(this.getNumberByName(parName), MathUtils.getDoubleArray(pars, this.names().asArray()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double derivValue(int n, double[] vector) throws NotDefinedException {
        if (multiFunc != null) {
            return multiFunc.derivValue(n, vector);
        } else {
            NamedVector set = new NamedVector(names.asArray(), vector);
            return nFunc.derivValue(names.asArray()[n], set);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getDimension() {
        return size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Names names() {
        return names;
    }

    private int getNumberByName(String name) {
        return this.names().asList().indexOf(name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Подразумевается, что аналитически заданы все(!) производные
     */
    @Override
    public boolean providesDeriv(int n) {
        if (nFunc != null && nFunc.providesDeriv(this.names().asArray()[n])) {
            return true;
        }
        return multiFunc != null && multiFunc.providesDeriv(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesDeriv(String name) {
        if (nFunc != null) {
            return nFunc.providesDeriv(name);
        } else {
            return multiFunc.providesDeriv(this.getNumberByName(name));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double value(NamedValueSet pars) {
        if (nFunc != null) {
            return nFunc.value(pars);
        } else {
            if (!pars.names().contains(names.asArray())) {
                throw new IllegalArgumentException("Wrong parameter set.");
            }
            return this.value(MathUtils.getDoubleArray(pars, this.names().asArray()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double value(double[] vector) {
        if (multiFunc != null) {
            return multiFunc.value(vector);
        } else {
            NamedVector set = new NamedVector(names.asArray(), vector);
            return nFunc.value(set);
        }
    }
}
