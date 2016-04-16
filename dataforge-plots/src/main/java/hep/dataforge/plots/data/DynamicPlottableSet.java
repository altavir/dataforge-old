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
package hep.dataforge.plots.data;

import hep.dataforge.points.DataPoint;
import hep.dataforge.values.Value;
import java.util.Iterator;

/**
 *
 * @author darksnake
 */
public class DynamicPlottableSet extends PlottableSet<DynamicPlottable> {


    public DynamicPlottableSet() {
    }

    public DynamicPlottableSet(DynamicPlottable... plottables) {
        super(plottables);
    }

    public DynamicPlottableSet(Iterable<DynamicPlottable> plottables) {
        super(plottables);
    }

    public void put(DataPoint point) {
        for (String name : map.keySet()) {
            if (point.hasValue(name)) {
                map.get(name).put(point.getValue(name));
            }
        }
    }

    public void put(String name, Object value) {
        put(name, Value.of(value));
    }

    public void put(String name, Value value) {
        if (map.containsKey(name)) {
            map.get(name).put(value);
        }
    }

    /**
     * Maximum age in millis
     *
     * @param millis
     */
    public void setMaxAge(int millis) {
        setEachConfigValue("maxAge", Value.of(millis));
    }
    
    public void setMaxItems(int maxItems){
        setEachConfigValue("maxItems", Value.of(maxItems));
    }

    @Override
    public Iterator<DynamicPlottable> iterator() {
        return this.map.values().iterator();
    }
}
