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

import hep.dataforge.datafitter.Param;
import hep.dataforge.datafitter.ParamSet;
import static java.lang.Math.abs;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.util.Precision;

/**
 * <p>DerivativeCalculator class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class DerivativeCalculator {

    private static final int numPoints = 3;

    /**
     * Calculates finite differences derivative via 3 points differentiator.
     *
     * @param function a {@link hep.dataforge.functions.NamedFunction} object.
     * @param point a {@link hep.dataforge.datafitter.ParamSet} object.
     * @param parName a {@link java.lang.String} object.
     * @return a double.
     */
    public static double calculateDerivative(NamedFunction function, ParamSet point, String parName) {
        UnivariateFunction projection = FunctionUtils.getNamedProjection(function, parName, point);
        Param par = point.getByName(parName);
        FiniteDifferencesDifferentiator diff
                = new FiniteDifferencesDifferentiator(numPoints, par.getErr() / 2.0d, par.getLowerBound(), par.getUpperBound());
        UnivariateDifferentiableFunction derivative = diff.differentiate(projection);
        DerivativeStructure x = new DerivativeStructure(1, 1, 0, point.getValue(parName));
        DerivativeStructure y = derivative.value(x);
        return y.getPartialDerivative(1);
    }
    
    /**
     * Calculates finite differences derivative via 3 points differentiator.
     *
     * @param function a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param point a double.
     * @param step a double.
     * @return a double.
     */
    public static double calculateDerivative(UnivariateFunction function, double point, double step) {
        FiniteDifferencesDifferentiator diff
                = new FiniteDifferencesDifferentiator(numPoints, step);
        UnivariateDifferentiableFunction derivative = diff.differentiate(function);
        DerivativeStructure x = new DerivativeStructure(1, 1, 0, point);
        DerivativeStructure y = derivative.value(x);
        return y.getPartialDerivative(1);
    }

    /**
     * <p>providesValidDerivative.</p>
     *
     * @param function a {@link hep.dataforge.functions.NamedFunction} object.
     * @param point a {@link hep.dataforge.datafitter.ParamSet} object.
     * @param tolerance a double.
     * @param parName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean providesValidDerivative(NamedFunction function, ParamSet point, double tolerance, String parName) {
        if (!function.providesDeriv(parName)) {
            return false;
        }
        double calculatedDeriv = calculateDerivative(function, point, parName);
        double providedDeriv = function.derivValue(parName, point);
        return safeRelativeDifference(calculatedDeriv, providedDeriv) <= tolerance;
    }

    /**
     * Returns safe from (no devision by zero) relative difference between two
     * input values
     *
     * @param val1
     * @param val2
     * @return
     */
    private static double safeRelativeDifference(double val1, double val2) {
        if (Precision.equals(val1, val2, Precision.EPSILON)) {
            return 0;
        }
        double average = abs(val1 + val2) / 2;
        if (average > Precision.EPSILON) {
            return abs(val1 - val2) / average;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
}
