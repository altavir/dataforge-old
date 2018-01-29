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
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaMorph;
import hep.dataforge.names.Names;
import hep.dataforge.utils.GenericBuilder;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A simple {@link Values} implementation using HashMap.
 *
 * @author Alexander Nozik
 */

public class ValueMap implements Values, MetaMorph {

    public static ValueMap ofMap(Map<String, ?> map) {
        return new ValueMap(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Value.of(entry.getValue()))));
    }

    public static ValueMap ofPairs(Pair<String, ?>... pairs) {
        Builder builder = new Builder();
        for(Pair<String, ?> pair: pairs){
            builder.putValue(pair.getFirst(),Value.of(pair.getSecond()));
        }
        return builder.build();
    }

    public static ValueMap of(String[] list, Object... values) {
        if (list.length != values.length) {
            throw new IllegalArgumentException();
        }
        LinkedHashMap<String, Value> valueMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            Value val = Value.of(values[i]);
            valueMap.put(list[i], val);
        }
        return new ValueMap(valueMap);
    }


    private final LinkedHashMap<String, Value> valueMap = new LinkedHashMap<>();

    /**
     * Serialization constructor
     */
    public ValueMap(Meta meta) {
        meta.getValueNames().forEach(valName -> {
            valueMap.put(valName, meta.getValue(valName));
        });
    }

    public ValueMap(Map<String, Value> map) {
        this.valueMap.putAll(map);
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
    public Names getNames() {
        return Names.of(this.valueMap.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Value> optValue(@NotNull String name) throws NameNotFoundException {
        return Optional.ofNullable(valueMap.get(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("[");
        boolean flag = true;
        for (String name : this.getNames()) {
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
    public Meta toMeta() {
        MetaBuilder builder = new MetaBuilder("point");
        for (String name : namesAsArray()) {
            builder.putValue(name, getValue(name));
        }
        return builder.build();
    }

//    @Override
//    public Map<String, Value> asMap() {
//        return Collections.unmodifiableMap(this.valueMap);
//    }

    public static class Builder implements GenericBuilder<ValueMap, Builder> {

        private final LinkedHashMap<String, Value> valueMap = new LinkedHashMap<>();

        public Builder(Values dp) {
            for (String name : dp.getNames()) {
                valueMap.put(name, dp.getValue(name));
            }

        }

        public Builder(Map<String, Value> map) {
            valueMap.putAll(map);
        }

        public Builder() {

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
            valueMap.put(name, value);
            return this;
        }

        public Builder putValue(String name, Object value) {
            valueMap.put(name, Value.of(value));
            return this;
        }

        /**
         * Put the value at the beginning of the map
         *
         * @param name
         * @param value
         * @return
         */
        public Builder putFirstValue(String name, Object value) {
            synchronized (valueMap) {
                LinkedHashMap<String, Value> newMap = new LinkedHashMap<>();
                newMap.put(name, Value.of(value));
                newMap.putAll(valueMap);
                valueMap.clear();
                valueMap.putAll(newMap);
                return this;
            }
        }

        public Builder addTag(String tag) {
            return putValue(tag, true);
        }

        @Override
        public ValueMap build() {
            return new ValueMap(valueMap);
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}
