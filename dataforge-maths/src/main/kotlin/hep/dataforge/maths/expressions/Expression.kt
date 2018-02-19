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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths.expressions

import hep.dataforge.names.Names
import hep.dataforge.values.Value
import hep.dataforge.values.Values

/**
 * The expression that could be evaluated in runtime
 *
 * @author Alexander Nozik
 */
interface Expression {

    /**
     * A set of names for parameters of this expression
     */
    val names: Names

    /**
     * Evaluate expression using given set of parameters.
     * The provided set of parameters could be broader then requested set. Also expression could provide defaults for
     * some values, in this case exception is not thrown even if one of such parameters is missing.
     * @return
     */
    operator fun invoke(parameters: Values): Value
}


class PreCompiledExpression{

}