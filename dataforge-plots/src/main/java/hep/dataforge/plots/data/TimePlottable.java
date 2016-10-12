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

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.MapPoint;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;

/**
 * A plottable to display dynamic series with limited number of elements (x axis is always considered to be time). Both
 * criteria are used to eviction of old elements
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "maxAge", type = "NUMBER", def = "-1", info = "The maximum age of items in milliseconds. 0 means no limit")
@ValueDef(name = "maxItems", type = "NUMBER", def = "-1", info = "The maximum number of items. 0 means no limit")
public class TimePlottable extends XYPlottable {

    public static final String DEFAULT_TIMESTAMP_KEY = "timestamp";

    private final DataMap map = new DataMap();
    private final String timestamp;
    private final String yName;
    private Instant lastUpdate;

    /**
     * Create dynamic plottable with given y value name (x name is always
     * "timestamp")
     *
     * @param name
     * @param yName
     */
    public TimePlottable(String name, String timestamp, String yName) {
        super(name);
        super.setAdapter(new XYAdapter(timestamp, yName));
        this.timestamp = timestamp;
        this.yName = yName;
    }

    /**
     * Use default timestamp key for timestamp name
     * @param name
     * @param yName
     */
    public TimePlottable(String name, String yName) {
        this(name, DEFAULT_TIMESTAMP_KEY, yName);
    }


    /**
     * Use yName for plottable name
     * @param yName
     */
    public TimePlottable(String yName) {
        this(yName, DEFAULT_TIMESTAMP_KEY, yName);
    }

    public static TimePlottable build(String name, String yName, String color, double thickness) {
        TimePlottable res = new TimePlottable(name, yName);
        res.getConfig()
                .setValue("color", color)
                .setValue("thickness", thickness);
        return res;
    }

    /**
     * Puts value with the same name as this y name from data point. If data
     * point contains time, it is used, otherwise current time is used.
     *
     * @param point
     */
    public void put(DataPoint point) {
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
        put(Instant.now(), value);
    }

    /**
     * Put time-value pair
     *
     * @param time
     * @param value
     */
    public void put(Instant time, Value value) {
        Map<String, Value> point = new HashMap<>(2);
        point.put(timestamp, Value.of(time));
        point.put(yName, value);
        this.map.put(time, new MapPoint(point));
        if (lastUpdate == null || time.isAfter(lastUpdate)) {
            lastUpdate = time;
        }
        notifyDataChanged();
    }

    @Override
    protected List<DataPoint> getRawData(Meta query) {
        return new ArrayList<>(map.values());
    }

    public String getYName() {
        return yName;
    }

    public Instant getLastUpdateTime() {
        return lastUpdate;
    }

    public void setMaxItems(int maxItems) {
        getConfig().setValue("maxItems", maxItems);
    }

    public void setMaxAge(Duration age) {
        getConfig().setValue("maxAge", age.toMillis());
    }

    public int size() {
        return map.size();
    }

    private class DataMap extends LinkedHashMap<Instant, DataPoint> {

        @Override
        protected boolean removeEldestEntry(Entry<Instant, DataPoint> eldest) {
            int maxItems = meta().getInt("maxItems", -1);
            if (maxItems > 0 && size() > maxItems) {
                return true;
            }
            int maxAge = meta().getInt("maxAge", -1);
            if (maxAge > 0 && lastUpdate != null && Duration.between(eldest.getKey(), lastUpdate).toMillis() > maxAge) {
                return true;
            }
            return false;
        }

    }

}