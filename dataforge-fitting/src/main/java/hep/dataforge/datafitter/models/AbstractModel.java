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
package hep.dataforge.datafitter.models;

import hep.dataforge.content.NamedMetaHolder;
import hep.dataforge.data.DataPoint;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.functions.AbstractNamedFunction;
import hep.dataforge.functions.NamedFunction;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.names.NamedSet;
import hep.dataforge.names.Names;
import hep.dataforge.data.PointAdapter;

/**
 * <p>
 * Abstract AbstractModel class.</p>
 *
 * @author Alexander Nozik
 * @param <T>
 */
public abstract class AbstractModel<T extends PointAdapter> extends NamedMetaHolder implements Model {
//TODO add default parameters to model
    private final Names names;

    /**
     *
     */
    protected T adapter;

    protected AbstractModel(String name, Names names, T adapter) {
        super(name);
        this.adapter = adapter;
        this.names = names;
    }

    protected AbstractModel(String name, NamedSet source, T adapter) {
        super(name);
        this.adapter = adapter;
        this.names = source.names();
    }

    public final void setAdapter(T adapter) {
        this.adapter = adapter;
    }

    public T getAdapter() {
        return adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDimension() {
        return names.getDimension();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedFunction getDistanceFunction(DataPoint point) {
        return new AbstractNamedFunction(names) {

            @Override
            public double derivValue(String derivParName, NamedDoubleSet pars) throws NotDefinedException, NamingException {
                return disDeriv(derivParName, point, pars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return AbstractModel.this.providesDeriv(name);
            }

            @Override
            public double value(NamedDoubleSet pars) throws NamingException {
                return distance(point, pars);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedFunction getLogProbFunction(DataPoint point) {
        if (!providesProb()) {
            throw new IllegalStateException("Model does not provide internal probability distribution");
        }

        return new AbstractNamedFunction(names) {
            @Override
            public double derivValue(String derivParName, NamedDoubleSet pars) throws NotDefinedException, NamingException {
                return getLogProbDeriv(derivParName, point, pars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return AbstractModel.this.providesProbDeriv(name);
            }

            @Override
            public double value(NamedDoubleSet pars) throws NamingException {
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
