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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A wrapper for value lists which could be used both as listValue and as value.
 * When used as value only first element of listValue is used. If the listValue
 * is empty, than ValueSet is equivalent of Null value.
 *
 * @author Alexander Nozik
 */
public class ValueSet implements Value {

    private final List<Value> values;

    public ValueSet(Collection<Object> values) {
        this.values = new ArrayList<>();
        values.stream().forEach((o) -> {
            this.values.add(Value.of(o));
        });
    }

    public ValueSet(Value... values) {
        this.values = Arrays.asList(values);
    }

    @Override
    public Number numberValue() {
        if (values.size() > 0) {
            return values.get(0).numberValue();
        } else {
            return 0;
        }
    }

    @Override
    public boolean booleanValue() {
        if (values.size() > 0) {
            return values.get(0).booleanValue();
        } else {
            return false;
        }
    }

    @Override
    public Instant timeValue() {
        if (values.size() > 0) {
            return values.get(0).timeValue();
        } else {
            return Instant.ofEpochMilli(0);
        }
    }

    @Override
    public String stringValue() {
        if (values.isEmpty()) {
            return "";
        } else if (values.size() == 1) {
            return values.get(0).stringValue();
        } else {
            return values.stream().<String>map(v -> v.stringValue()).collect(Collectors.joining(", ", "[", "]"));
        }
    }

    @Override
    public ValueType valueType() {
        if (values.size() > 0) {
            return values.get(0).valueType();
        } else {
            return ValueType.NULL;
        }
    }

    @Override
    public List<Value> listValue() {
        return this.values;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.values);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValueSet other = (ValueSet) obj;
        return Objects.equals(this.values, other.values);
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public String toString() {
        return stringValue();
    }

}
