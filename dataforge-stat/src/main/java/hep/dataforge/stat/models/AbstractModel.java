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
package hep.dataforge.stat.models;

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Names;
import hep.dataforge.stat.parametric.AbstractParametricValue;
import hep.dataforge.stat.parametric.ParametricValue;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.PointAdapter;
import hep.dataforge.utils.BaseMetaHolder;
import hep.dataforge.values.NamedValueSet;

/**
 * Basic implementation for model
 *
 * @author Alexander Nozik
 * @param <T>
 */
public abstract class AbstractModel<T extends PointAdapter> extends BaseMetaHolder implements Model {
//TODO add default parameters to model

    private final Names names;

    /**
     *
     */
    protected T adapter;

    protected AbstractModel(Names names, T adapter) {
        this.adapter = adapter;
        this.names = names;
    }

    protected AbstractModel(NameSetContainer source, T adapter) {
        this.adapter = adapter;
        this.names = source.names();
    }

    public T getAdapter() {
        return adapter;
    }

    public final void setAdapter(T adapter) {
        this.adapter = adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return names.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParametricValue getDistanceFunction(DataPoint point) {
        return new AbstractParametricValue(names) {

            @Override
            public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException, NamingException {
                return disDeriv(derivParName, point, pars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return AbstractModel.this.providesDeriv(name);
            }

            @Override
            public double value(NamedValueSet pars) throws NamingException {
                return distance(point, pars);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParametricValue getLogProbFunction(DataPoint point) {
        if (!providesProb()) {
            throw new IllegalStateException("Model does not provide internal probability distribution");
        }

        return new AbstractParametricValue(names) {
            @Override
            public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException, NamingException {
                return getLogProbDeriv(derivParName, point, pars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return AbstractModel.this.providesProbDeriv(name);
            }

            @Override
            public double value(NamedValueSet pars) throws NamingException {
                return getLogProb(point, pars);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Names names() {
        return names;
    }
}
