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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.NameSetContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * A named set of values with fixed name list.
 */
public interface Values extends NameSetContainer, ValueProvider {

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

    /**
     * Convert a DataPoint to a Map. Order is not guaranteed
     * @return
     */
    default Map<String,Value> asMap(){
        Map<String,Value> res = new HashMap<>();
        for(String field: this.names()){
            res.put(field, getValue(field));
        }
        return res;
    }

    /**
     * Simple check for boolean tag
     *
     * @param name
     * @return
     */
    default boolean hasTag(String name) {
        return names().contains(name) && getValue(name).booleanValue();
    }

    default Meta toMeta() {
        MetaBuilder builder = new MetaBuilder("point");
        for (String name : namesAsArray()) {
            builder.putValue(name, getValue(name));
        }
        return builder.build();
    }
}
