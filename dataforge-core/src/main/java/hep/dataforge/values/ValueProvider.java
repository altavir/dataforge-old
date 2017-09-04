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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.providers.Path;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface ValueProvider {

    String VALUE_TARGET = "value";
    String STRING_TARGET = "string";
    String NUMBER_TARGET = "number";
    String BOOLEAN_TARGET = "boolean";
    String TIME_TARGET = "time";

    /**
     * Build a meta provider from given general provider
     *
     * @param provider
     * @return
     */
    static ValueProvider buildFrom(Provider provider) {
        if (provider instanceof ValueProvider) {
            return (ValueProvider) provider;
        }
        return path -> provider.provide(Path.of(path, VALUE_TARGET)).map(Value.class::cast);
    }

    default boolean hasValue(String path) {
        return optValue(path).isPresent();
    }

    @Provides(VALUE_TARGET)
    Optional<Value> optValue(String path);

    default Value getValue(String path) {
        return optValue(path).orElseThrow(() -> new NameNotFoundException(path));
    }

    @Provides(BOOLEAN_TARGET)
    default Optional<Boolean> optBoolean(String name) {
        return optValue(name).map(Value::booleanValue);
    }


    default Boolean getBoolean(String name, boolean def) {
        return optValue(name).map(Value::booleanValue).orElse(def);
    }

    default Boolean getBoolean(String name, Supplier<Boolean> def) {
        return optValue(name).map(Value::booleanValue).orElseGet(def);
    }

    default Boolean getBoolean(String name) {
        return getValue(name).booleanValue();
    }

    @Provides(NUMBER_TARGET)
    default Optional<Number> optNumber(String name) {
        return optValue(name).map(Value::numberValue);
    }

    default Double getDouble(String name, double def) {
        return optValue(name).map(Value::doubleValue).orElse(def);
    }

    default Double getDouble(String name, Supplier<Double> def) {
        return optValue(name).map(Value::doubleValue).orElseGet(def);
    }

    default Double getDouble(String name) {
        return getValue(name).doubleValue();
    }

    default Integer getInt(String name, int def) {
        return optValue(name).map(Value::intValue).orElse(def);
    }

    default Integer getInt(String name, Supplier<Integer> def) {
        return optValue(name).map(Value::intValue).orElseGet(def);

    }

    default Integer getInt(String name) {
        return getValue(name).intValue();
    }

    @Provides(STRING_TARGET)
    default Optional<String> optString(String name) {
        return optValue(name).map(Value::stringValue);
    }

    default String getString(String name, String def) {
        return optString(name).orElse(def);
    }

    default String getString(String name, Supplier<String> def) {
        return optString(name).orElseGet(def);
    }

    default String getString(String name) {
        return getValue(name).stringValue();
    }

    default Value getValue(String name, Object def) {
        return optValue(name).orElse(Value.of(def));
    }

    default Value getValue(String name, Supplier<Value> def) {
        return optValue(name).orElseGet(def);
    }

    @Provides(TIME_TARGET)
    default Optional<Instant> optTime(String name) {
        return optValue(name).map(Value::timeValue);
    }

    default String[] getStringArray(String name) {
        List<Value> vals = getValue(name).listValue();
        String[] res = new String[vals.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = vals.get(i).stringValue();
        }
        return res;
    }

    default String[] getStringArray(String name, Supplier<String[]> def) {
        if (this.hasValue(name)) {
            return getStringArray(name);
        } else {
            return def.get();
        }
    }

    default String[] getStringArray(String name, String[] def) {
        if (this.hasValue(name)) {
            return getStringArray(name);
        } else {
            return def;
        }
    }
}
