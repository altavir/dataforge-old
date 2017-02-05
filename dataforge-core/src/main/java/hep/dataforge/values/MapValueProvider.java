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

import java.util.HashMap;
import java.util.Map;

/**
 * Simple valuew provider based on map
 * Created by darksnake on 16-Aug-16.
 */
public class MapValueProvider implements ValueProvider {
    private final Map<String, Value> map;

    public MapValueProvider(Map<String, ?> map) {
        this.map = new HashMap<>();
        map.forEach((key, value) -> this.map.put(key, Value.of(value)));
    }

    @Override
    public boolean hasValue(String path) {
        return map.containsKey(path);
    }

    @Override
    public Value getValue(String path) {
        return map.get(path);
    }
}
