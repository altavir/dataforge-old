/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.maths.functions

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import org.apache.commons.math3.exception.DimensionMismatchException
import org.apache.commons.math3.exception.OutOfRangeException

/**
 * A wrapper function for spline including valuew outside the spline region
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
class UnivariateSplineWrapper : UnivariateDifferentiableFunction {

    private val outOfRegionValue: Double
    internal var source: PolynomialSplineFunction

    /**
     *
     * Constructor for UnivariateSplineWrapper.
     *
     * @param source a [org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction] object.
     * @param outOfRegionValue a double.
     */
    constructor(source: PolynomialSplineFunction, outOfRegionValue: Double) {
        this.source = source
        this.outOfRegionValue = outOfRegionValue
    }

    /**
     *
     * Constructor for UnivariateSplineWrapper.
     *
     * @param source a [org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction] object.
     */
    constructor(source: PolynomialSplineFunction) {
        this.source = source
        this.outOfRegionValue = 0.0
    }


    /** {@inheritDoc}  */
    @Throws(DimensionMismatchException::class)
    override fun value(t: DerivativeStructure): DerivativeStructure {
        try {
            return source.value(t)
        } catch (ex: OutOfRangeException) {
            return DerivativeStructure(t.freeParameters, t.order, outOfRegionValue)
        }

    }

    /** {@inheritDoc}  */
    override fun value(x: Double): Double {
        try {
            return source.value(x)
        } catch (ex: OutOfRangeException) {
            return outOfRegionValue
        }

    }
}
