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

import hep.dataforge.tables.DataPoint;
import hep.dataforge.values.Value;

import java.time.Duration;
import java.util.Iterator;

/**
 * @author darksnake
 */
public class TimePlottableGroup extends PlottableGroup<TimePlottable> {

    public TimePlottableGroup() {
    }

    public TimePlottableGroup(TimePlottable... plottables) {
        super(plottables);
    }

    public TimePlottableGroup(Iterable<TimePlottable> plottables) {
        super(plottables);
    }

    public static TimePlottableGroup buildSet(String... names) {
        TimePlottableGroup set = new TimePlottableGroup();
        for (String name : names) {
            set.add(new TimePlottable(name, name));
        }
        return set;
    }

    public void put(DataPoint point) {
        map.keySet().stream().filter(point::hasValue).forEach(name -> {
            map.get(name).put(point.getValue(name));
        });
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
     * @param age
     */
    public void setMaxAge(Duration age) {
        this.forEach(it -> it.setMaxAge(age));
    }

    public void setMaxItems(int maxItems) {
        this.forEach(it -> it.setMaxItems(maxItems));
    }

    public void setPrefItems(int prefItems) {
        this.forEach(it -> it.setPrefItems(prefItems));
    }

    @Override
    public Iterator<TimePlottable> iterator() {
        return this.map.values().iterator();
    }
}
