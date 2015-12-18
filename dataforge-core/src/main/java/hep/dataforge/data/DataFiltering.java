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
package hep.dataforge.data;

import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import java.util.List;
import java.util.function.Predicate;

/**
 * <p>
 * DataFiltering class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class DataFiltering {

    /**
     * A simple condition that DataPoint has all presented tags
     *
     * @param tags a {@link java.lang.String} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    public static Predicate<DataPoint> getTagCondition(final String... tags) {
        return (DataPoint dp) -> {
            boolean pass = true;
            for (String tag : tags) {
                pass = pass & dp.hasTag(tag);
            }
            return pass;
        };
    }

    /**
     * Simple condition that field with name {@code valueName} is from a to b.
     * Both could be infinite.
     *
     * @param valueName a {@link java.lang.String} object.
     * @param a a {@link hep.dataforge.values.Value} object.
     * @param b a {@link hep.dataforge.values.Value} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    public static Predicate<DataPoint> getValueCondition(final String valueName, final Value a, final Value b) {
        if (a.compareTo(b) >= 0) {
            throw new IllegalArgumentException();
        }
        return (DataPoint dp) -> {
            if (!dp.names().contains(valueName)) {
                return false;
            } else {
                try {
                    return (dp.getValue(valueName).compareTo(a) >= 0) && (dp.getValue(valueName).compareTo(b) <= 0);
                } catch (NameNotFoundException ex) {
                    //Считаем, что если такого имени нет, то тест автоматически провален
                    return false;
                }
            }
        };
    }

    public static Predicate<DataPoint> getValueEqualityCondition(final String valueName, final Value equals) {
        return (DataPoint dp) -> {
            if (!dp.names().contains(valueName)) {
                return false;
            } else {
                try {
                    return (dp.getValue(valueName).equals(equals));
                } catch (NameNotFoundException ex) {
                    //Считаем, что если такого имени нет, то тест автоматически провален
                    return false;
                }
            }
        };
    }

    /**
     * <p>
     * buildConditionSet.</p>
     *
     * @param an a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    @NodeDef(name = "is", multiple = true, info = "The filtering condition that must be satisfied", target = "method::hep.dataforge.data.DataFiltering.buildCondition")
    @NodeDef(name = "not", multiple = true, info = "The filtering condition that must NOT be satisfied", target = "method::hep.dataforge.data.DataFiltering.buildCondition")
    public static Predicate<DataPoint> buildConditionSet(Meta an) {
        Predicate<DataPoint> res = null;
        if (an.hasNode("is")) {
            for (Meta condition : an.getNodes("is")) {
                Predicate<DataPoint> predicate = buildCondition(condition);
                if (res == null) {
                    res = predicate;
                } else {
                    res = res.or(predicate);
                }
            }
        }

        if (an.hasNode("not")) {
            for (Meta condition : an.getNodes("not")) {
                Predicate<DataPoint> predicate = buildCondition(condition).negate();
                if (res == null) {
                    res = predicate;
                } else {
                    res = res.or(predicate);
                }
            }
        }
        return res;
    }

    /**
     * <p>
     * buildCondition.</p>
     *
     * @param an a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    @ValueDef(name = "tag", info = "The DataPoint tag to filter")
    @ValueDef(name = "value", info = "The value name to filter")
    @ValueDef(name = "equals", info = "The equality condition value")
    @ValueDef(name = "from", type = "NUMBER", info = "The lower bound for value. The 'value' parameter should be present")
    @ValueDef(name = "to", type = "NUMBER", info = "The upper bound for value. The 'value' parameter should be present")
    public static Predicate<DataPoint> buildCondition(Meta an) {
        Predicate<DataPoint> res = null;
        if (an.hasValue("tag")) {
            List<Value> tagList = an.getValue("tag").listValue();
            String[] tags = new String[tagList.size()];
            for (int i = 0; i < tagList.size(); i++) {
                tags[i] = tagList.get(i).stringValue();
            }
            res = getTagCondition(tags);
        }
        if (an.hasValue("value")) {
            String valueName = an.getValue("value").stringValue();
            Predicate<DataPoint> valueCondition;
            if (an.hasValue("equals")) {
                Value equals = an.getValue("equals");
                valueCondition = getValueEqualityCondition(valueName, equals);
            } else {
                Value from = an.getValue("from", Value.of(Double.NEGATIVE_INFINITY));
                Value to = an.getValue("to", Value.of(Double.POSITIVE_INFINITY));
                valueCondition = getValueCondition(valueName, from, to);
            }

            if (res == null) {
                res = valueCondition;
            } else {
                res = res.or(valueCondition);
            }
        }
        return res;
    }
}
