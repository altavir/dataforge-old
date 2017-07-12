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

import hep.dataforge.utils.NamingUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * An immutable wrapper class that can hold Numbers, Strings and Instant
 * objects. The general contract for Value is that it is immutable, more
 * specifically, it can't be changed after first call (it could be lazy
 * calculated though)
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Value extends Serializable {
    String NULL_STRING = "@null";

    Value NULL = new NullValue();

    /**
     * Create Value from String using closest match conversion
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    static Value of(String str) {
        //Trying to get integer
        if (str == null || str.isEmpty()) {
            return Value.getNull();
        }

        try {
            int val = Integer.parseInt(str);
            return of(val);
        } catch (NumberFormatException ex) {
        }

        //Trying to get double        
        try {
            double val = Double.parseDouble(str);
            return of(val);
        } catch (NumberFormatException ex) {
        }

        //Trying to get Instant
        try {
            Instant val = Instant.parse(str);
            return of(val);
        } catch (DateTimeParseException ex) {
        }

        //Trying to parse LocalDateTime
        try {
            Instant val = LocalDateTime.parse(str).toInstant(ZoneOffset.UTC);
            return of(val);
        } catch (DateTimeParseException ex) {
        }

        if ("true".equals(str) || "false".equals(str)) {
            return BooleanValue.ofBoolean(str);
        }

        if (str.startsWith("[") && str.endsWith("]")) {
            //FIXME there will be a problem with nested lists
            String[] strings = NamingUtils.parseArray(str);
            return Value.of(strings);
        }

        //Give up and return a StringValue
        return new StringValue(str);
    }

    static Value of(String[] strings) {
        List<Value> values = new ArrayList<>();
        for (String str : strings) {
            values.add(Value.of(str));
        }
        return Value.of(values);
    }

    /**
     * create a boolean Value
     *
     * @param b a boolean.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    static Value of(boolean b) {
        return BooleanValue.ofBoolean(b);
    }

    static Value of(double d) {
        return new NumberValue(d);
    }

    static Value of(int i) {
        return new NumberValue(i);
    }

    static Value of(long l) {
        return new NumberValue(l);
    }

    static Value of(BigDecimal bd) {
        return new NumberValue(bd);
    }

    static Value of(LocalDateTime t) {
        return new TimeValue(t);
    }

    static Value of(Instant t) {
        return new TimeValue(t);
    }

    static Value of(Object... list) {
        return of(Arrays.asList(list));
    }

    static Value of(Collection<Object> list) {
        List<Object> l = new ArrayList<>();
        l.addAll(list);
        if (l.isEmpty()) {
            return getNull();
        } else if (l.size() == 1) {
            return of(l.get(0));
        } else {
            return new ListValue(l);
        }
    }

    static Value of(Value... list) {
        switch (list.length) {
            case 0:
                return getNull();
            case 1:
                return list[0];
            default:
                return new ListValue(list);
        }
    }

    static Value getNull() {
        return NULL;
    }

    /**
     * Create Value from any object using closest match conversion. Throws a
     * RuntimeException if given object could not be converted to Value
     *
     * @param obj a {@link java.lang.Object} object.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    @SuppressWarnings("unchecked")
    static Value of(Object obj) {
        if (obj == null) {
            return Value.NULL;
        }
        if (obj instanceof Number) {
            return new NumberValue((Number) obj);
        } else if (obj instanceof Instant) {
            return of((Instant) obj);
        } else if (obj instanceof LocalDateTime) {
            return new TimeValue((LocalDateTime) obj);
        } else if (obj instanceof Boolean) {
            return BooleanValue.ofBoolean((boolean) obj);
        } else if (obj instanceof Value) {
            //это можно делать так как Value неизменяемый
            return (Value) obj;
        } else if (obj instanceof String) {
            return of((String) obj);
        } else if (obj instanceof Collection) {
            return of((Collection) obj);
        } else if (obj instanceof Object[]) {
            return of((Object[]) obj);
        } else {
            return of(obj.toString());
        }
    }

    /**
     * The number representation of this value
     *
     * @return a {@link java.lang.Number} object.
     */
    Number numberValue();

    /**
     * Boolean representation of this Value
     *
     * @return a boolean.
     */
    boolean booleanValue();

    default double doubleValue() {
        return numberValue().doubleValue();
    }

    default int intValue() {
        return numberValue().intValue();
    }

    default long longValue() {
        return numberValue().longValue();
    }

    /**
     * Instant representation of this Value
     *
     * @return
     */
    Instant timeValue();

    /**
     * The String representation of this value
     *
     * @return a {@link java.lang.String} object.
     */
    String stringValue();

    @NotNull
    ValueType getType();

    /**
     * Return underlining object. Used for dynamic calls mostly
     */
    Object value();

    /**
     * Return list of values representation of current value. If value is
     * instance of ListValue, than the actual list is returned, otherwise
     * immutable singleton list is returned.
     *
     * @return
     */
    default List<Value> listValue() {
        return Collections.singletonList(this);
    }

    default boolean isNull() {
        return this.getType().equals(ValueType.NULL);
    }

    /**
     * True if it is a list value
     *
     * @return
     */
    default boolean isList() {
        return false;
    }

}
