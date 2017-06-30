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
package hep.dataforge.tables;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NonEmptyMetaMorphException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Names;
import hep.dataforge.utils.GenericBuilder;
import hep.dataforge.utils.MetaMorph;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A simple {@link Values} implementation using HashMap.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */

public class ValueMap implements Values, MetaMorph {

    public static ValueMap fromMap(Map<String, Object> map) {
        return new ValueMap(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Value.of(entry.getValue()))));
    }

    private Map<String, Value> valueMap;

    /**
     * Serialization constructor
     */
    public ValueMap() {
        this.valueMap = new LinkedHashMap<>();
    }

    public ValueMap(String[] list, Number... values) {
        if (list.length != values.length) {
            throw new IllegalArgumentException();
        }
        this.valueMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            valueMap.put(list[i], Value.of(values[i]));
        }
    }

    public ValueMap(String[] list, Value... values) {
        if (list.length != values.length) {
            throw new IllegalArgumentException();
        }
        this.valueMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            Value val = values[i];
            valueMap.put(list[i], val);
        }
    }

    public ValueMap(String[] list, Object[] values) {
        if (list.length != values.length) {
            throw new IllegalArgumentException();
        }
        this.valueMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            Value val = Value.of(values[i]);

            valueMap.put(list[i], val);
        }
    }

    public ValueMap(Map<String, Value> map) {
        this.valueMap = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return valueMap.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasValue(String path) {
        return this.valueMap.containsKey(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Names names() {
        return Names.of(this.valueMap.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Value> optValue(String name) throws NameNotFoundException {
        return Optional.ofNullable(valueMap.get(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("[");
        boolean flag = true;
        for (String name : this.names()) {
            if (flag) {
                flag = false;
            } else {
                res.append(", ");
            }
            res.append(name).append(":").append(getValue(name).stringValue());
        }
        return res + "]";
    }

    public Builder builder() {
        return new Builder(new LinkedHashMap<>(valueMap));
    }

    @Override
    public void fromMeta(Meta meta) {
        if (!this.valueMap.isEmpty()) {
            throw new NonEmptyMetaMorphException(getClass());
        }
        meta.getValueNames().forEach(valName -> {
            valueMap.put(valName, meta.getValue(valName));
        });
    }

    @Override
    public Meta toMeta() {
        MetaBuilder builder = new MetaBuilder("point");
        for (String name : namesAsArray()) {
            builder.putValue(name, getValue(name));
        }
        return builder.build();
    }

    @Override
    public Map<String, Value> asMap() {
        return Collections.unmodifiableMap(this.valueMap);
    }

    public static class Builder implements GenericBuilder<ValueMap, Builder> {

        private final ValueMap p;

        public Builder(Values dp) {
            p = new ValueMap(new LinkedHashMap<>(dp.names().size()));
            for (String name : dp.names()) {
                p.valueMap.put(name, dp.getValue(name));
            }

        }

        public Builder(Map<String, Value> map) {
            p = new ValueMap(map);
        }

        public Builder() {
            p = new ValueMap();
        }

        /**
         * if value exists it is replaced
         *
         * @param name  a {@link java.lang.String} object.
         * @param value a {@link hep.dataforge.values.Value} object.
         * @return a {@link ValueMap} object.
         */
        public Builder putValue(String name, Value value) {
            if (value == null) {
                value = Value.NULL;
            }
            p.valueMap.put(name, value);
            return this;
        }

        public Builder putValue(String name, Object value) {
            p.valueMap.put(name, Value.of(value));
            return this;
        }

        public Builder addTag(String tag) {
            return putValue(tag, true);
        }

        @Override
        public ValueMap build() {
            return p;
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}
