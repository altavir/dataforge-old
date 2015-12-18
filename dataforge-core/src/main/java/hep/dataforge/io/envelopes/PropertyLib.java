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
package hep.dataforge.io.envelopes;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.values.CompositePropertyValue;
import hep.dataforge.values.Value;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 * A library that maps property codes and values to corresponding pre-generated
 * objects
 *
 * @author Alexander Nozik
 */
public class PropertyLib<T> {

    Map<Value, T> objectMap = new HashMap<>();

    /**
     * Put an object with composite int-string value as key
     *
     * @param code
     * @param value
     * @param object
     */
    public void putComposite(int code, String value, T object) {
        this.objectMap.put(new CompositePropertyValue(code, value), object);
    }

    public void put(Value key, T object) {
        this.objectMap.put(key, object);
    }

    public void put(String key, T object) {
        this.objectMap.put(Value.of(key), object);
    }

    public Value findValue(int code) {
        for (Value v : objectMap.keySet()) {
            if (v.intValue() == code) {
                return v;
            }
        }
        return Value.of(code);
    }

    public Value findValue(String name) {
        for (Value v : objectMap.keySet()) {
            if (v.stringValue().equals(name)) {
                return v;
            }
        }
        LoggerFactory.getLogger(getClass()).debug("Can't find property with name {}. Returning as-is value", name);
        return Value.of(name);
    }

    public T getDefault() {
        throw new NotDefinedException("The default key is not defined");
    }

    public T get(Value key) {
        for (Value v : this.objectMap.keySet()) {
            if (v.equals(key)) {
                return objectMap.get(v);
            }
        }
        LoggerFactory.getLogger(getClass()).warn("Can't find object with key {}. Using default object", key.stringValue());
        return getDefault();

    }

    public T get(String key) {
        return get(Value.of(key));
    }

    public T get(int code) {
        return get(Value.of(code));
    }

    public Set<Value> keySet() {
        return objectMap.keySet();
    }

}
