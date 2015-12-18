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

import hep.dataforge.exceptions.ValueConversionException;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A special composite value that returns code if asked for integer and string
 * if asked for string.
 *
 * @author Alexander Nozik
 */
public class CompositePropertyValue implements Value {

    private final int code;
    private final String value;

    public CompositePropertyValue(int code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public Number numberValue() {
        return code;
    }

    @Override
    public boolean booleanValue() {
        return code != 0;
    }

    @Override
    public Instant timeValue() {
        throw new ValueConversionException(this, ValueType.TIME);
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public ValueType valueType() {
        return ValueType.STRING;
    }

    @Override
    public int compareTo(Value t) {
        return Integer.compare(this.code, t.intValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.code;
        hash = 79 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Value)) {
            return false;
        }
        Value val = (Value) obj;
        if (val.valueType() == ValueType.STRING) {
            return this.value.equals(val.stringValue());
        } else {
            return this.code == val.intValue();
        }
    }

}
