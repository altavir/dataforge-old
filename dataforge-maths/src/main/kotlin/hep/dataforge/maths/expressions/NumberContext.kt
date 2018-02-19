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

interface NumberContext<N : Number> {
    fun plus(a: N, b: N): N
    fun minus(a: N, b: N): N
    fun div(a: N, b: N): N
    fun times(a: N, b: N): N
    //fun reminder(a: N, b: N): N
    //fun unaryMinus(a: N): N
}

interface ExtendedNumberContext<N: Number>: NumberContext<N>{

}