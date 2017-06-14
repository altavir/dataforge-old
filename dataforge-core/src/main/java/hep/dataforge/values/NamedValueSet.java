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

import hep.dataforge.names.NameSetContainer;

/**
 * A value provider with declared ordered list of names
 */
public interface NamedValueSet extends NameSetContainer, ValueProvider {

    /**
     * Faster search for existing values
     *
     * @param path
     * @return
     */
    @Override
    default boolean hasValue(String path) {
        return this.names().contains(path);
    }

    /**
     * A convenient method to access value by its index. Has generally worse performance.
     *
     * @param num
     * @return
     */
    default Value getAt(int num) {
        return getValue(this.names().get(num));
    }
}
