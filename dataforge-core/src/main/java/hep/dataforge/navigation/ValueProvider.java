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
package hep.dataforge.navigation;

import hep.dataforge.values.Value;

/**
 * <p>ValueProvider interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface ValueProvider {
    /**
     * <p>hasValue.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean hasValue(String path);
    /**
     * <p>getValue.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    Value getValue(String path);
    
    //PENDING добавить возможность возырвщать подпровайдера?
    
    /**
     * <p>
     * getBoolean.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param def a boolean.
     * @return a boolean.
     */
    default boolean getBoolean(String name, boolean def) {
        if (this.hasValue(name)) {
            return getValue(name).booleanValue();
        } else {
            return def;
        }
    }

    /**
     * <p>
     * getBoolean.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    default boolean getBoolean(String name) {
        return getValue(name).booleanValue();
    }

    /**
     * <p>
     * getDouble.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param def a double.
     * @return a double.
     */
    default double getDouble(String name, double def) {
        if (this.hasValue(name)) {
            return getValue(name).doubleValue();
        } else {
            return def;
        }
    }

    /**
     * <p>
     * getDouble.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a double.
     */
    default double getDouble(String name) {
        return getValue(name).doubleValue();
    }

    /**
     * <p>
     * getInt.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param def a int.
     * @return a int.
     */
    default int getInt(String name, int def) {
        if (this.hasValue(name)) {
            return getValue(name).intValue();
        } else {
            return def;
        }
    }

    /**
     * <p>
     * getInt.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a int.
     */
    default int getInt(String name) {
        return getValue(name).intValue();
    }

    /**
     * <p>
     * getString.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param def a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    default String getString(String name, String def) {
        if (this.hasValue(name)) {
            return getValue(name).stringValue();
        } else {
            return def;
        }
    }

    /**
     * <p>
     * getString.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    default String getString(String name) {
        return getValue(name).stringValue();
    }

    /**
     * <p>
     * getValue.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param def a {@link hep.dataforge.values.Value} object.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    default Value getValue(String name, Value def) {
        if (this.hasValue(name)) {
            return getValue(name);
        } else {
            return def;
        }
    }    
}
