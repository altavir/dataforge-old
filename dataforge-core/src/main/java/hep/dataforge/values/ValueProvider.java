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

import java.util.List;
import java.util.function.Supplier;

public interface ValueProvider {

    String VALUE_TARGET = "value";

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
        return new ValueProvider() {
            @Override
            public Value getValue(String path) {
                return provider.provide(Path.of(path, VALUE_TARGET), Value.class);
            }

            @Override
            public boolean hasValue(String path) {
                return provider.provides(Path.of(path, VALUE_TARGET));
            }

        };
    }

    default boolean hasValue(String path) {
        try {
            return getValue(path) != null;
        } catch (NameNotFoundException ex) {
            return false;
        }
    }

    Value getValue(String path);

    default Boolean getBoolean(String name, boolean def) {
        if (this.hasValue(name)) {
            return getValue(name).booleanValue();
        } else {
            return def;
        }
    }

    default Boolean getBoolean(String name, Supplier<Boolean> def) {
        if (this.hasValue(name)) {
            return getValue(name).booleanValue();
        } else {
            return def.get();
        }
    }

    default Boolean getBoolean(String name) {
        return getValue(name).booleanValue();
    }

    default Double getDouble(String name, double def) {
        if (this.hasValue(name)) {
            return getValue(name).doubleValue();
        } else {
            return def;
        }
    }

    default Double getDouble(String name, Supplier<Double> def) {
        if (this.hasValue(name)) {
            return getValue(name).doubleValue();
        } else {
            return def.get();
        }
    }

    default Double getDouble(String name) {
        return getValue(name).doubleValue();
    }

    default Integer getInt(String name, int def) {
        if (this.hasValue(name)) {
            return getValue(name).intValue();
        } else {
            return def;
        }
    }

    default Integer getInt(String name, Supplier<Integer> def) {
        if (this.hasValue(name)) {
            return getValue(name).intValue();
        } else {
            return def.get();
        }
    }

    default Integer getInt(String name) {
        return getValue(name).intValue();
    }

    default String getString(String name, String def) {
        if (this.hasValue(name)) {
            return getValue(name).stringValue();
        } else {
            return def;
        }
    }

    default String getString(String name, Supplier<String> def) {
        if (this.hasValue(name)) {
            return getValue(name).stringValue();
        } else {
            return def.get();
        }
    }

    default String getString(String name) {
        return getValue(name).stringValue();
    }

    default Value getValue(String name, Value def) {
        if (this.hasValue(name)) {
            return getValue(name);
        } else {
            return def;
        }
    }

    default Value getValue(String name, Supplier<Value> def) {
        if (this.hasValue(name)) {
            return getValue(name);
        } else {
            return def.get();
        }
    }

    default String[] getStringArray(String name) {
        List<Value> vals = getValue(name).listValue();
        String[] res = new String[vals.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = vals.get(i).stringValue();
        }
        return res;
    }

    default  String[] getStringArray(String name, Supplier<String[]> def) {
        if (this.hasValue(name)) {
            return getStringArray(name);
        } else {
            return def.get();
        }
    }
}
