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

import hep.dataforge.meta.Meta
import org.apache.commons.math3.analysis.UnivariateFunction
import java.util.*


/**
 * A univariate function cache that does not recalculate function if the distance between nearest points is less then precison and uses linear interpolation instead
 */
class CachedFunction(val precision: Double, val function: (Double) -> Double) : UnivariateFunction {
    private val cache: TreeMap<Double, Double> = TreeMap()

    override fun value(x: Double): Double {
        val floor = cache.floorEntry(x)
        val ciel = cache.ceilingEntry(x)

        return if (floor == null || ciel == null || ciel.key - floor.key < precision) {
            function(x).also {
                cache[x] = it
            }
        } else {
            interpolate(x, floor, ciel)
        }
    }

    private fun interpolate(x: Double, floor: Map.Entry<Double, Double>, ciel: Map.Entry<Double, Double>): Double {
        return floor.value + (x - floor.key) * (ciel.value - floor.value) / (ciel.key - ciel.key)
    }
}

/**
 *
 * Cache values of given function on-demand
 *
 * @param a a double.
 * @param b a double.
 * @param numCachePoints a int.
 * @param func a [UnivariateFunction] object.
 * @return a [org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction] object.
 */
fun UnivariateFunction.cache(precision: Double): UnivariateFunction {
    return CachedFunction(precision) { this.value(it) }
}

object Functions {

    fun parabola(): (Meta) -> (Double) -> Double {
        return { meta ->
            val a = meta.getDouble("a", 1.0)
            val b = meta.getDouble("b", 0.0)
            val c = meta.getDouble("b", 0.0)
            val res: (Double) -> Double = { x -> a * x * x + b * x + c }
            res
        }
    }

    fun polynomial(): (Meta) -> (Double) -> Double {
        return { meta ->
            val coefs = meta.getValue("coef").listValue()
            val res = { x: Double ->
                var sum = 0.0
                var curX = 1.0
                for (i in coefs.indices) {
                    sum += coefs.get(i).doubleValue() * curX
                    curX *= x
                }
                sum
            }
            res
        }
    }
}