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

package hep.dataforge.maths.expressions

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sqrt

class ADTest {
    @Test
    fun testNormalAD() {
        val field = ADField(1, "amp", "pos", "sigma")
        val x = 0
        val gauss = with(field) {
            val amp = variable("amp", 1)
            val pos = variable("pos", 0)
            val sigma = variable("sigma", 1)
            amp / (sigma * sqrt(2 * PI)) * exp(-(pos - x).pow(2) / sigma.pow(2) / 2)
        }

        assertEquals(1.0 / sqrt(2.0 * PI), gauss.toDouble(), 0.001)
        assertEquals(0.0,gauss.deriv("pos"),0.001)
    }
}