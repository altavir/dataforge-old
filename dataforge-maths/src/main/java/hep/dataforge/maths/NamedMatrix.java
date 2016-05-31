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

import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Square named matrix. Не обязательно симметричная, но обзательно квадратная.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class NamedMatrix implements NameSetContainer {

    /**
     * Create diagonal named matrix from given named double set
     *
     * @param vector
     * @return
     */
    public static NamedMatrix diagonal(NamedValueSet vector) {
        double[] vectorValues = MathUtils.getDoubleArray(vector);
        double[][] values = new double[vectorValues.length][vectorValues.length];
        for (int i = 0; i < vectorValues.length; i++) {
            values[i][i] = vectorValues[i];
        }
        return new NamedMatrix(values, vector.namesAsArray());
    }

    //TODO заменить на массив
    private Array2DRowRealMatrix mat;
    private final Names names;

    /**
     * <p>
     * Constructor for NamedMatrix.</p>
     *
     * @param mat a {@link org.apache.commons.math3.linear.RealMatrix} object.
     * @param names an array of {@link java.lang.String} objects.
     */
    public NamedMatrix(RealMatrix mat, String[] names) {
        this.names = Names.of(names);
        if (!mat.isSquare()) {
            throw new IllegalArgumentException("Only square matrices allowed.");
        }
        if (mat.getColumnDimension() != names.length) {
            throw new DimensionMismatchException(mat.getColumnDimension(), names.length);
        }
        this.mat = new Array2DRowRealMatrix(mat.getData(), true);
    }

    /**
     * <p>
     * Constructor for NamedMatrix.</p>
     *
     * @param values an array of double.
     * @param names an array of {@link java.lang.String} objects.
     */
    public NamedMatrix(double[][] values, String[] names) {
        this.names = Names.of(names);
        if (values.length != values[0].length) {
            throw new IllegalArgumentException("Only square matrices allowed.");
        }
        if (values.length != names.length) {
            throw new DimensionMismatchException(values.length, names.length);
        }
        this.mat = new Array2DRowRealMatrix(values, true);
    }

    /**
     * <p>
     * copy.</p>
     *
     * @return a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public NamedMatrix copy() {
        return new NamedMatrix(getMatrix().copy(), this.namesAsArray());
    }

    double getElement(int i, int j) {
        return mat.getEntry(i, j);
    }

    /**
     * <p>
     * getElement.</p>
     *
     * @param name1 a {@link java.lang.String} object.
     * @param name2 a {@link java.lang.String} object.
     * @return a double.
     */
    public double getElement(String name1, String name2) {
        return mat.getEntry(this.names.getNumberByName(name1), this.names.getNumberByName(name2));
    }

    /**
     * <p>
     * getMatrix.</p>
     *
     * @return a {@link org.apache.commons.math3.linear.RealMatrix} object.
     */
    public RealMatrix getMatrix() {
        return this.mat;
    }

    /**
     * Return named submatrix with given names. The order of names in submatrix
     * is the one provided by arguments.
     *
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public NamedMatrix getNamedSubMatrix(String... names) {
        if (!this.names().contains(names)) {
            throw new IllegalArgumentException();
        }
        int[] numbers = new int[names.length];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = this.names.getNumberByName(names[i]);

        }
        RealMatrix newMat = this.mat.getSubMatrix(numbers, numbers);
        return new NamedMatrix(newMat, names);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Names names() {
        return names;
    }

    /**
     * <p>
     * setElement.</p>
     *
     * @param name1 a {@link java.lang.String} object.
     * @param name2 a {@link java.lang.String} object.
     * @param value a double.
     */
    public void setElement(String name1, String name2, double value) {
        mat.setEntry(this.names.getNumberByName(name1), this.names.getNumberByName(name2), value);
    }

    /**
     * update values of this matrix from corresponding values of given named
     * matrix. The order of columns does not matter.
     *
     * @param matrix a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public void setValuesFrom(NamedMatrix matrix) {
        for (int i = 0; i < matrix.getDimension(); i++) {
            for (int j = 0; j < matrix.getDimension(); j++) {
                String name1 = matrix.names.getName(i);
                String name2 = matrix.names.getName(j);
                if (names.contains(name1) && names.contains(name2)) {
                    this.setElement(name1, name2, matrix.getElement(i, j));
                }
            }

        }
    }

    /**
     * <p>
     * getRow.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.apache.commons.math3.linear.RealVector} object.
     */
    public RealVector getRow(String name) {
        return getMatrix().getRowVector(names.getNumberByName(name));
    }

    /**
     * <p>
     * getColumn.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.apache.commons.math3.linear.RealVector} object.
     */
    public RealVector getColumn(String name) {
        return getMatrix().getColumnVector(names.getNumberByName(name));
    }
}
