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
package hep.dataforge.maths;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;
import hep.dataforge.values.Value;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Именованый вектор. Данные хранятся в виде массива. Скорость доступа поностью
 * зависит от скорости NameList.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class NamedVector implements NamedValueSet {

    Names nameList;
    ArrayRealVector vector;

    public NamedVector(String[] names, RealVector v) {
        if (names.length != v.getDimension()) {
            throw new IllegalArgumentException();
        }
        vector = new ArrayRealVector(v);
        this.nameList = Names.of(names);
    }

    public NamedVector(String[] names, double[] d) {
        if (names.length != d.length) {
            throw new DimensionMismatchException(d.length, names.length);
        }
        vector = new ArrayRealVector(d);
        this.nameList = Names.of(names);
    }

    public NamedVector(Names names, double[] d) {
        if (names.size() != d.length) {
            throw new DimensionMismatchException(d.length, names.size());
        }
        vector = new ArrayRealVector(d);
        this.nameList = Names.of(names);
    }

    public NamedVector(NamedValueSet set) {
        vector = new ArrayRealVector(MathUtils.getDoubleArray(set));
        this.nameList = Names.of(set.names());
    }

    @Override
    public Value getValue(String path) {
        return Value.of(getDouble(path));
    }    
    
    public NamedVector copy() {
        return new NamedVector(this.namesAsArray(), vector);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public int size() {
        return this.nameList.size();
    }

    /**
     * {@inheritDoc}
     *
     * @param name
     */
    @Override
    public Double getDouble(String name) {
        //TODO максимально усклоить эту операцию

        int n = this.getNumberByName(name);
        if (n < 0) {
            throw new NameNotFoundException(name);
        }
        return vector.getEntry(n);
    }

    public int getNumberByName(String name) {
        return nameList.asList().indexOf(name);
    }

    /**
     * {@inheritDoc}
     */
    public double[] getArray(String... names) {
        if (names.length == 0) {
            return vector.toArray();
        } else {
            if (!this.names().contains(names)) {
                throw new NamingException();
            }
            double[] res = new double[names.length];
            for (int i = 0; i < names.length; i++) {
                res[i] = vector.getEntry(this.getNumberByName(names[i]));
            }
            return res;
        }
    }

    public ArrayRealVector getVector() {
        return vector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Names names() {
        return nameList;
    }

    public void setValue(String name, double val) {
        int n = this.getNumberByName(name);
        if (n < 0) {
            throw new NameNotFoundException(name);
        }

        vector.setEntry(n, val);
    }
}
