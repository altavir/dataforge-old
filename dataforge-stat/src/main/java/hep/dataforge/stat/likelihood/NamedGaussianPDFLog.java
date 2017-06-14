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
import hep.dataforge.stat.parametric.AbstractParametricValue;
import hep.dataforge.values.NamedValueSet;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;

import static java.lang.Math.*;

/**
 * <p>
 * NamedGaussianPDFLog class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class NamedGaussianPDFLog extends AbstractParametricValue {

    private final NamedMatrix infoMatrix;
    private final double norm;
    private final RealVector values;

    public NamedGaussianPDFLog(NamedValueSet values, NamedMatrix covariance) {
        super(covariance);
        this.values = this.getVector(values);
        LUDecomposition decomposition = new LUDecomposition(covariance.getMatrix());
        double det = decomposition.getDeterminant();
        this.infoMatrix = new NamedMatrix(values.namesAsArray(), decomposition.getSolver().getInverse());
        norm = 1 / sqrt(det) / pow(2 * Math.PI, this.size() / 2d);
    }

    public NamedGaussianPDFLog(NamedMatrix infoMatrix) {
        super(infoMatrix);
        this.values = new ArrayRealVector(infoMatrix.size());
        LUDecomposition decomposition = new LUDecomposition(infoMatrix.getMatrix());
        double det = decomposition.getDeterminant();
        this.infoMatrix = infoMatrix;
        norm = sqrt(det) / pow(2 * Math.PI, this.size() / 2d);
    }

    @Override
    public double derivValue(String derivParName, NamedValueSet pars) {
        RealVector difVector = getVector(pars).subtract(values);
        RealVector c = this.infoMatrix.getRow(derivParName).getVector();

        double res = -difVector.dotProduct(c);
        return res;
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public double expValue(NamedValueSet point) {
//        RealVector difVector = getVector(point).subtract(values);
//        double expValue = infoMatrix.getMatrix().preMultiply(difVector).dotProduct(difVector) / 2;
//        return norm * exp(-expValue);
//    }

    /**
     * Создаем неименованый вектор, с теми, что нам нужны.
     *
     * @param set
     * @return
     */
    private RealVector getVector(NamedValueSet set) {
        ArrayRealVector vector = new ArrayRealVector(this.size());
        String[] namesArray = namesAsArray();
        for (int i = 0; i < this.size(); i++) {
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
