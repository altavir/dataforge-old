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
package hep.dataforge.values;

import hep.dataforge.values.ValueProvider;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value provider with substitutions
 *
 * @author Alexander Nozik
 */
public abstract class SubstProvider implements ValueProvider {

    /**
     * {@inheritDoc}
     *
     * @param name
     */
    @Override
    public Value getValue(String name) {
        Value val = getValueForName(name);
        if (val.valueType().equals(ValueType.STRING) && val.stringValue().contains("$")) {
            String valStr = val.stringValue();
            Matcher matcher = Pattern.compile("\\$\\{(?<sub>.*)\\}").matcher(valStr);
            while (matcher.find()) {
                valStr = valStr.replace(matcher.group(), evaluateSubst(matcher.group("sub")));
            }
            return Value.of(valStr);
        } else {
            return val;
        }
    }

    /**
     * Provide the value for name, where name is taken literally
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    protected abstract Value getValueForName(String name);

    /**
     * Evaluate substitution string for ${} query.
     *
     * TODO сделать что-то более умное вроде GString
     *
     * @param subst a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String evaluateSubst(String subst) {
        return getValueForName(subst).stringValue();
    }

}
