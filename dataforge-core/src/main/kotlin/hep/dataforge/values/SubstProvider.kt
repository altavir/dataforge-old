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
package hep.dataforge.values

import java.util.regex.Pattern

/**
 * Value provider with substitutions
 *
 * @author Alexander Nozik
 */
abstract class SubstProvider : ValueProvider {

    /**
     * {@inheritDoc}
     *
     * @param name
     */
    override fun getValue(name: String): Value {
        val `val` = getValueForName(name)
        if (`val`.type == ValueType.STRING && `val`.string.contains("$")) {
            var valStr = `val`.string
            val matcher = Pattern.compile("\\$\\{(?<sub>.*)\\}").matcher(valStr)
            while (matcher.find()) {
                valStr = valStr.replace(matcher.group(), evaluateSubst(matcher.group("sub")))
            }
            return Value.of(valStr)
        } else {
            return `val`
        }
    }

    /**
     * Provide the value for name, where name is taken literally
     *
     * @param name a [String] object.
     * @return a [Value] object.
     */
    protected abstract fun getValueForName(name: String): Value

    /**
     * Evaluate substitution string for ${} query.
     *
     * TODO сделать что-то более умное вроде GString
     *
     * @param subst a [String] object.
     * @return a [String] object.
     */
    protected fun evaluateSubst(subst: String): String {
        return getValueForName(subst).string
    }

}
