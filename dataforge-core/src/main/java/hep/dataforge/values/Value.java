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

import hep.dataforge.content.Content;
import hep.dataforge.utils.NamingUtils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An immutable wrapper class that can hold Numbers, Strings and Instant
 * objects. The general contract for Value is that it is immutable, more
 * specifically, it can't be changed after first call (it could be lazy
 * calculated though)
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Value extends Comparable<Value>, Serializable {
    
    public static final Value NULL = new NullValue();

    /**
     * Create Value from String using closest match conversion
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    public static Value of(String str) {
        //Trying to get integer
        if (str == null) {
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
            return new BooleanValue(str);
        }

        if (str.startsWith("[") && str.endsWith("]")) {
            //FIXME there will be a problem with nested lists
            String[] strings = NamingUtils.parseArray(str);
            return Value.of(strings);
        }

        //Give up and return a StringValue
        return new StringValue(str);
    }

    public static Value of(String[] strings) {
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
    public static Value of(boolean b) {
        return new BooleanValue(b);
    }

    public static Value of(double d) {
        return new NumberValue(d);
    }

    public static Value of(int i) {
        return new NumberValue(i);
    }

    public static Value of(long l) {
        return new NumberValue(l);
    }

    public static Value of(BigDecimal bd) {
        return new NumberValue(bd);
    }

    public static Value of(LocalDateTime t) {
        return new TimeValue(LocalDateTime.from(t));
    }

    public static Value of(Instant t) {
        return new TimeValue(LocalDateTime.ofInstant(t, ZoneId.systemDefault()));
    }

    public static Value of(Collection<Object> list) {
        List<Object> l = new ArrayList<>();
        l.addAll(list);
        if (l.isEmpty()) {
            return getNull();
        } else if (l.size() == 1) {
            return of(l.get(0));
        } else {
            return new ValueSet(l);
        }
    }

    public static Value of(Value... list) {
        switch (list.length) {
            case 0:
                return getNull();
            case 1:
                return list[0];
            default:
                return new ValueSet(list);
        }
    }

    public static Value getNull() {
        return NULL;
    }

    /**
     * Create Value from any object using closest match conversion. Throws a
     * RuntimeException if given object could not be converted to Value
     *
     * @param obj a {@link java.lang.Object} object.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    public static Value of(Object obj) {
        if (obj == null) {
            return new NullValue();
        }
        if (obj instanceof Number) {
            return new NumberValue((Number) obj);
        } else if (obj instanceof Instant) {
            return of((Instant) obj);
        } else if (obj instanceof LocalDateTime) {
            return new TimeValue((LocalDateTime) obj);
        } else if (obj instanceof Boolean) {
            return new BooleanValue((boolean) obj);
        } else if (obj instanceof Value) {
            //это можно делать так как Value неизменяемый
            return (Value) obj;
        } else if (obj instanceof Content) {
            return of((Content) obj);
        } else if (obj instanceof String) {
            return of((String) obj);
        } else if (obj instanceof Collection) {
            return of((Collection) obj);
        } else {
            //сделать Content обертку?
            throw new RuntimeException("Can not get a Value for " + obj.getClass().getName());
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

    ValueType valueType();

    /**
     * Checks if given value is between {@code val1} and {@code val1}. Could
     * throw ValueConversionException if value conversion is not possible.
     *
     * @param val1
     * @param val2
     * @return
     */
    default boolean isBetween(Value val1, Value val2) {
        return (compareTo(val1) > 0 && compareTo(val2) < 0)
                || (compareTo(val2) > 0 && compareTo(val1) < 0);
    }

    /**
     * Return list of values representation of current value. If value is
     * instance of ValueSet, than the actual list is returned, otherwise
     * immutable singleton list is returned.
     *
     * @return
     */
    default List<Value> listValue() {
        return Collections.singletonList(this);
    }

    default boolean isNull() {
        return this.valueType().equals(ValueType.NULL);
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
