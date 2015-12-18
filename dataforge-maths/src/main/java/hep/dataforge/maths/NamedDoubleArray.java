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
public class NamedDoubleArray implements NamedDoubleSet {

    Names nameList;
    ArrayRealVector vector;

    /**
     * <p>Constructor for NamedDoubleArray.</p>
     *
     * @param names an array of {@link java.lang.String} objects.
     * @param v a {@link org.apache.commons.math3.linear.RealVector} object.
     */
    public NamedDoubleArray(String[] names, RealVector v) {
        if (names.length != v.getDimension()) {
            throw new IllegalArgumentException();
        }
        vector = new ArrayRealVector(v);
        this.nameList = Names.of(names);
    }

    /**
     * <p>Constructor for NamedDoubleArray.</p>
     *
     * @param names an array of {@link java.lang.String} objects.
     * @param d an array of double.
     */
    public NamedDoubleArray(String[] names, double[] d) {
        if (names.length != d.length) {
            throw new DimensionMismatchException(d.length, names.length);
        }
        vector = new ArrayRealVector(d);
        this.nameList = Names.of(names);
    }
    
    /**
     * <p>Constructor for NamedDoubleArray.</p>
     *
     * @param names a {@link hep.dataforge.names.Names} object.
     * @param d an array of double.
     */
    public NamedDoubleArray(Names names, double[] d) {
        if (names.getDimension() != d.length) {
            throw new DimensionMismatchException(d.length, names.getDimension());
        }
        vector = new ArrayRealVector(d);
        this.nameList = Names.of(names);
    }    

    /**
     * <p>Constructor for NamedDoubleArray.</p>
     *
     * @param set a {@link hep.dataforge.maths.NamedDoubleSet} object.
     */
    public NamedDoubleArray(NamedDoubleSet set) {
        vector = new ArrayRealVector(set.getValues());        
        this.nameList = Names.of(set.names());
    }
    
    /**
     * <p>copy.</p>
     *
     * @return a {@link hep.dataforge.maths.NamedDoubleArray} object.
     */
    public NamedDoubleArray copy() {
        return new NamedDoubleArray(this.namesAsArray(), vector);
    }

    /** {@inheritDoc}
     * @return  */
    @Override
    public int getDimension() {
        return this.nameList.getDimension();
    }

    /** {@inheritDoc}
     * @param name */
    @Override
    public double getValue(String name) {
        //TODO максимально усклоить эту операцию

        int n = this.getNumberByName(name);
        if (n < 0) {
            throw new NameNotFoundException(name);
        }
        return vector.getEntry(n);
    }

    /**
     * <p>getNumberByName.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a int.
     */
    public int getNumberByName(String name) {
        return nameList.asList().indexOf(name);
    }

    /** {@inheritDoc} */
    @Override
    public double[] getValues(String... names) {
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

    /**
     * <p>Getter for the field <code>vector</code>.</p>
     *
     * @return a {@link org.apache.commons.math3.linear.ArrayRealVector} object.
     */
    public ArrayRealVector getVector(){
        return vector;
    }

    /** {@inheritDoc} */
    @Override
    public Names names() {
        return nameList;
    }
    
    /**
     * <p>setValue.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param val a double.
     */
    public void setValue(String name, double val) {
        int n = this.getNumberByName(name);
        if (n < 0) {
            throw new NameNotFoundException(name);
        }

        vector.setEntry(n, val);
    }
}
