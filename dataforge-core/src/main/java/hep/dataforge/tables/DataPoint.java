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
import hep.dataforge.values.NamedValueSet;
import hep.dataforge.values.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A name-value mapping with some additional features
 */
public interface DataPoint extends NamedValueSet, Serializable {

    static List<DataPoint> buildFromMeta(Meta annotation) {
        List<DataPoint> res = new ArrayList<>();
        for (Meta pointAn : annotation.getMetaList("point")) {
            Map<String, Value> map = new HashMap<>();
            for (String key : pointAn.getValueNames()) {
                map.put(key, pointAn.getValue(key));
            }
            res.add(new MapPoint(map));
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Value getValue(String name) throws NameNotFoundException;

    @Override
    default boolean hasValue(String path) {
        return names().contains(path);
    }

    /**
     * Метод для удобной фильтрации по булевым тэгам
     *
     * @param name
     * @return
     */
    default boolean hasTag(String name) {
        if (!names().contains(name)) {
            return false;
        } else {
            return getValue(name).booleanValue();
        }
    }

    default Meta toMeta() {
        MetaBuilder builder = new MetaBuilder("point");
        for (String name : namesAsArray()) {
            builder.putValue(name, getValue(name));
        }
        return builder.build();
    }

}
