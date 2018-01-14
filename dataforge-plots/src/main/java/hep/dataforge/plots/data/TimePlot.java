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

import hep.dataforge.meta.Meta;
import hep.dataforge.plots.Plottable;
import hep.dataforge.tables.Adapters;
import hep.dataforge.tables.ValueMap;
import hep.dataforge.utils.DateTimeUtils;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * A plottable to display dynamic series with limited number of elements (x axis is always considered to be time). Both
 * criteria are used to eviction of old elements
 *
 * @author Alexander Nozik
 */
public class TimePlot extends XYPlot {

    public static void setMaxItems(Plottable plot, int maxItems) {
        plot.configureValue(MAX_ITEMS_KEY, maxItems);
    }

    public static void setMaxAge(Plottable plot, Duration age) {
        plot.configureValue(MAX_AGE_KEY, age.toMillis());
    }

    public static void setPrefItems(Plottable plot, int prefItems) {
        plot.configureValue(PREF_ITEMS_KEY, prefItems);
    }

    public static void put(Plottable plot, Object value){
        if(plot instanceof TimePlot){
            ((TimePlot) plot).put(Value.of(value));
        } else {
            LoggerFactory.getLogger(TimePlot.class).warn("Trying to put value TimePlot value into different plot");
        }
    }

    public static final String MAX_AGE_KEY = "maxAge";
    public static final String MAX_ITEMS_KEY = "maxItems";
    public static final String PREF_ITEMS_KEY = "prefItems";

    public static final String DEFAULT_TIMESTAMP_KEY = "timestamp";

    private TreeMap<Instant, Values> map = new TreeMap<>();
    private final String timestamp;
    private final String yName;

    /**
     * Create dynamic time plottable with given y value name
     *
     * @param name
     * @param yName
     */
    public TimePlot(String name, String timestamp, String yName) {
        super(name);
        super.setAdapter(Adapters.buildXYAdapter(timestamp, yName));
        this.timestamp = timestamp;
        this.yName = yName;
    }

    /**
     * Use default timestamp key for timestamp name
     *
     * @param name
     * @param yName
     */
    public TimePlot(String name, String yName) {
        this(name, DEFAULT_TIMESTAMP_KEY, yName);
    }


    /**
     * Use yName for plottable name
     *
     * @param yName
     */
    public TimePlot(String yName) {
        this(yName, DEFAULT_TIMESTAMP_KEY, yName);
    }

    /**
     * Puts value with the same name as this y name from data point. If data
     * point contains time, it is used, otherwise current time is used.
     *
     * @param point
     */
    public void put(Values point) {
        Value v = point.getValue(yName);
        if (point.hasValue(timestamp)) {
            put(point.getValue(timestamp).timeValue(), v);
        } else {
            put(v);
        }
    }

    /**
     * Put value with current time
     *
     * @param value
     */
    public void put(Value value) {
        put(DateTimeUtils.now(), value);
    }

    /**
     * Put time-value pair
     *
     * @param time
     * @param value
     */
    public synchronized void put(Instant time, Value value) {
        Map<String, Value> point = new HashMap<>(2);
        point.put(timestamp, Value.of(time));
        point.put(yName, value);
        this.map.put(time, new ValueMap(point));

        if (size() > 2) {
            int maxItems = getConfig().getInt(MAX_ITEMS_KEY, -1);
            int prefItems = getConfig().getInt(PREF_ITEMS_KEY, Math.min(400, maxItems));
            int maxAge = getConfig().getInt(MAX_AGE_KEY, -1);
            cleanup(maxAge, maxItems, prefItems);
        }

        notifyDataChanged();
    }

    @Override
    protected List<Values> getRawData(Meta query) {
        return new ArrayList<>(map.values());
    }

    public String getYName() {
        return yName;
    }

    public void setMaxItems(int maxItems) {
        getConfig().setValue(MAX_ITEMS_KEY, maxItems);
    }

    public void setMaxAge(Duration age) {
        getConfig().setValue(MAX_AGE_KEY, age.toMillis());
    }

    public void setPrefItems(int prefItems) {
        configureValue(PREF_ITEMS_KEY, prefItems);
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        this.map.clear();
        notifyDataChanged();
    }


    private synchronized void cleanup(int maxAge, int maxItems, int prefItems) {
        Instant first = map.firstKey();
        Instant last = map.lastKey();

        int oldsize = size();
        if (maxItems > 0 && oldsize > maxItems) {
            //copying retained elements into new map
            TreeMap<Instant, Values> newMap = new TreeMap<>();
            int step = (int) (Duration.between(first, last).toMillis() / prefItems);
            newMap.put(first, map.firstEntry().getValue());
            newMap.put(last, map.lastEntry().getValue());
            for (Instant x = first; x.isBefore(last); x = x.plusMillis(step)) {
                Map.Entry<Instant, Values> entry = map.ceilingEntry(x);
                newMap.putIfAbsent(entry.getKey(), entry.getValue());
            }
            //replacing map with new one
            this.map = newMap;
            LoggerFactory.getLogger(getClass()).debug("Reduced size from {} to {}", oldsize, size());
        }

        while (maxAge > 0 && last != null && Duration.between(map.firstKey(), last).toMillis() > maxAge) {
            map.remove(map.firstKey());
        }
    }


}
