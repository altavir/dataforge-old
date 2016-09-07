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
package hep.dataforge.stat.likelihood;

import hep.dataforge.maths.NamedMatrix;
import hep.dataforge.values.NamedValueSet;
import hep.dataforge.values.ValueProvider;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;

/**
 * <p>
 * NamedGaussianPDFLog class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class NamedGaussianPDFLog extends ScaleableNamedFunction {

    private final NamedMatrix infoMatrix;
    private final double norm;
    private final RealVector values;

    /**
     * <p>
     * Constructor for NamedGaussianPDFLog.</p>
     *
     * @param values a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @param covariance a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public NamedGaussianPDFLog(NamedValueSet values, NamedMatrix covariance) {
        super(covariance);
        this.values = this.getVector(values);
        LUDecomposition decomposition = new LUDecomposition(covariance.getMatrix());
        double det = decomposition.getDeterminant();
        this.infoMatrix = new NamedMatrix(decomposition.getSolver().getInverse(), values.namesAsArray());
        norm = 1 / sqrt(det) / pow(2 * Math.PI, this.getDimension() / 2d);
    }

    /**
     * <p>
     * Constructor for NamedGaussianPDFLog.</p>
     *
     * @param infoMatrix a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public NamedGaussianPDFLog(NamedMatrix infoMatrix) {
        super(infoMatrix);
        this.values = new ArrayRealVector(infoMatrix.getDimension());
        LUDecomposition decomposition = new LUDecomposition(infoMatrix.getMatrix());
        double det = decomposition.getDeterminant();
        this.infoMatrix = infoMatrix;
        norm = sqrt(det) / pow(2 * Math.PI, this.getDimension() / 2d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double derivValue(String derivParName, NamedValueSet pars) {
        RealVector difVector = getVector(pars).subtract(values);
        RealVector c = this.infoMatrix.getRow(derivParName);

        double res = -difVector.dotProduct(c);
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double expValue(NamedValueSet point) {
        RealVector difVector = getVector(point).subtract(values);
        double expValue = infoMatrix.getMatrix().preMultiply(difVector).dotProduct(difVector) / 2;
        return norm * exp(-expValue);
    }

    /**
     * Создаем неименованый вектор, с теми, что нам нужны.
     *
     * @param set
     * @return
     */
    private RealVector getVector(NamedValueSet set) {
        ArrayRealVector vector = new ArrayRealVector(this.getDimension());
        String[] namesArray = namesAsArray();
        for (int i = 0; i < this.getDimension(); i++) {
            vector.setEntry(i, set.getDouble(namesArray[i]));
        }
        return vector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesDeriv(String name) {
        return true;
    }

    @Override
    public double value(NamedValueSet pars) {
        RealVector difVector = getVector(pars).subtract(values);
        return log(norm) - infoMatrix.getMatrix().preMultiply(difVector).dotProduct(difVector) / 2;
    }
}