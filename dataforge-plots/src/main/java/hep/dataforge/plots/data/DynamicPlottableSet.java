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

import hep.dataforge.data.DataPoint;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.values.Value;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author darksnake
 */
public class DynamicPlottableSet implements Iterable<DynamicPlottable> {

    private final Map<String, DynamicPlottable> map = new LinkedHashMap<>();

    public DynamicPlottableSet() {
    }

    public DynamicPlottableSet(DynamicPlottable... plottables) {
        for (DynamicPlottable pl : plottables) {
            map.put(pl.getName(), pl);
        }
    }

    public DynamicPlottableSet(Iterable<DynamicPlottable> plottables) {
        for (DynamicPlottable pl : plottables) {
            map.put(pl.getName(), pl);
        }
    }

    public void addPlottable(DynamicPlottable pl) {
        map.put(pl.getName(), pl);
    }

    public void removePlottable(DynamicPlottable pl) {
        map.remove(pl.getName());
    }

    public DynamicPlottable getPlottable(String name) {
        return map.get(name);
    }

    public boolean hasPlottable(String name) {
        return map.containsKey(name);
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
     * Apply given configuration to each plottable
     *
     * @param config
     */
    public void applyEachConfig(Meta config) {
        for (DynamicPlottable pl : map.values()) {
            pl.updateConfig(config);
        }
    }

    /**
     * Set configuration value for each plottable
     *
     * @param name
     * @param value
     */
    public void setEachConfigValue(String name, Value value) {
        for (DynamicPlottable pl : map.values()) {
            pl.getConfig().setValue(name, value);
        }
    }

    /**
     * Apply configuration to plottables considering each plottable described
     * with appropriate {@code plot} node.
     * 
     * <p>
     *  A node marked {@code eachPlot} is applied to each plottable previously to individual configurations.
     * </p>
     *
     * @param config
     */
    public void applyConfig(Meta config) {
        if(config.hasNode("eachPlot")){
            applyEachConfig(config.getNode("eachPlot"));
        }
        for (DynamicPlottable pl : map.values()) {
            Meta m = MetaUtils.findNodeByValue(config, "plot", "name", pl.getName());
            if (m != null) {
                pl.updateConfig(m);
            }
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

    @Override
    public Iterator<DynamicPlottable> iterator() {
        return this.map.values().iterator();
    }
}
