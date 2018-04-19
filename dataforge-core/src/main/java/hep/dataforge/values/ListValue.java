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

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A wrapper for value lists which could be used both as listValue and as value.
 * When used as value only first element of listValue is used. If the listValue
 * is empty, than ListValue is equivalent of Null value.
 *
 * @author Alexander Nozik
 */
public class ListValue implements Value {

    private final List<Value> values;

    public ListValue(Collection<Object> values) {
        this.values = new ArrayList<>();
        values.forEach((o) -> {
            this.values.add(Value.of(o));
        });
    }

    public ListValue(Value... values) {
        this.values = Arrays.asList(values);
    }

    @Override
    public Number getNumber() {
        if (values.size() > 0) {
            return values.get(0).getNumber();
        } else {
            return 0;
        }
    }

    @Override
    public boolean getBoolean() {
        return values.size() > 0 && values.get(0).getBoolean();
    }

    @Override
    public Instant getTime() {
        if (values.size() > 0) {
            return values.get(0).getTime();
        } else {
            return Instant.ofEpochMilli(0);
        }
    }

    @Override
    public String getString() {
        if (values.isEmpty()) {
            return "";
        } else if (values.size() == 1) {
            return values.get(0).getString();
        } else {
            return values.stream().map(Value::getString).collect(Collectors.joining(", ", "[", "]"));
        }
    }

    @NotNull
    @Override
    public ValueType getType() {
        if (values.size() > 0) {
            return values.get(0).getType();
        } else {
            return ValueType.NULL;
        }
    }

    @Override
    public List<Value> getList() {
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
        final ListValue other = (ListValue) obj;
        return Objects.equals(this.values, other.values);
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public Object value() {
        return Collections.unmodifiableList(this.values);
    }
}
