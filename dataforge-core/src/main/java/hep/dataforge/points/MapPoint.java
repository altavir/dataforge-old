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
package hep.dataforge.points;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Names;
import hep.dataforge.values.Value;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Реализация DataPoint на HashMap. В конструкторе дополнительно проверяется,
 * что все значения численные. Для нечисленных значений нужно использовать тэги
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class MapPoint implements DataPoint {

    private Map<String, Value> valueMap;

    /**
     * <p>
     * Constructor for MapDataPoint.</p>
     *
     * @param point a {@link hep.dataforge.points.DataPoint} object.
     */
    public MapPoint(DataPoint point) {
        this.valueMap = new LinkedHashMap<>(point.names().getDimension());
        for (String name : point.names()) {
            this.valueMap.put(name, point.getValue(name));
        }
    }

    /**
     * <p>Constructor for MapDataPoint.</p>
     */
    public MapPoint() {
        this.valueMap = new LinkedHashMap<>();
    }

    /**
     * <p>
     * Constructor for MapDataPoint.</p>
     *
     * @param list an array of {@link java.lang.String} objects.
     * @param values a {@link java.lang.Number} object.
     */
    public MapPoint(String[] list, Number... values) {
        if (list.length != values.length) {
            throw new IllegalArgumentException();
        }
        this.valueMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            valueMap.put(list[i], Value.of(values[i]));
        }
    }

    /**
     * <p>
     * Constructor for MapDataPoint.</p>
     *
     * @param list an array of {@link java.lang.String} objects.
     * @param values a {@link hep.dataforge.values.Value} object.
     */
    public MapPoint(String[] list, Value... values) {
        if (list.length != values.length) {
            throw new IllegalArgumentException();
        }
        this.valueMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            Value val = values[i];
//            if (val.valueType().equals(ValueType.STRING)
//                    || val.valueType().equals(ValueType.NULL)
//                    || val.valueType().equals(ValueType.BOOLEAN)) {
//                LoggerFactory.getLogger(getClass()).debug("Tying to add {} value to DataPoint", val.valueType().name());
//            }
            valueMap.put(list[i], val);
        }
    }

    /**
     * <p>
     * Constructor for MapDataPoint.</p>
     *
     * @param list an array of {@link java.lang.String} objects.
     * @param values an array of {@link java.lang.Object} objects.
     */
    public MapPoint(String[] list, Object[] values) {
        if (list.length != values.length) {
            throw new IllegalArgumentException();
        }
        this.valueMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            Value val = Value.of(values[i]);
//            if (val.valueType().equals(ValueType.STRING)
//                    || val.valueType().equals(ValueType.NULL)
//                    || val.valueType().equals(ValueType.BOOLEAN)) {
//                LoggerFactory.getLogger(getClass()).debug("Tying to add {} value to DataPoint", val.valueType().name());
//            }
            valueMap.put(list[i], val);
        }
    }

    /**
     * <p>
     * Constructor for MapDataPoint.</p>
     *
     * @param map a {@link java.util.Map} object.
     */
    public MapPoint(Map<String, Value> map) {
        this.valueMap = map;
    }

    /**
     * <p>
     * addTag.</p>
     *
     * @param tag a {@link java.lang.String} object.
     */
    public void addTag(String tag) {
        putValue(tag, true);
    }

    /** {@inheritDoc} */
    @Override
    public MapPoint copy() {
        return new MapPoint(this);
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return valueMap.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasValue(String path) {
        return this.valueMap.containsKey(path);
    }

    /** {@inheritDoc} */
    @Override
    public Names names() {
        return Names.of(this.valueMap.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public Value getValue(String name) throws NameNotFoundException {
        assert valueMap != null;
        Value res = valueMap.get(name);
        if (res == null) {
            throw new NameNotFoundException(name);
        } else {
            return res;
        }
    }

    /**
     * if value exists it is replaced
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link hep.dataforge.values.Value} object.
     * @return a {@link hep.dataforge.points.MapPoint} object.
     */
    public MapPoint putValue(String name, Value value) {
        if(value == null){
            value = Value.NULL;
        }
        this.valueMap.put(name, value);
        return this;
    }

    /**
     * <p>
     * putValue.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     * @return a {@link hep.dataforge.points.MapPoint} object.
     */
    public MapPoint putValue(String name, Object value) {
        this.valueMap.put(name, Value.of(value));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String res = "[";
        boolean flag = true;
        for (String name : this.names()) {
            if (flag) {
                flag = false;
            } else {
                res += ", ";
            }
            res += name + ":" + getValue(name).stringValue();
        }
        return res + "]";
    }

    /**
     * <p>join.</p>
     *
     * @param point a {@link hep.dataforge.points.DataPoint} object.
     * @return a {@link hep.dataforge.points.MapPoint} object.
     */
    public MapPoint join(DataPoint point) {
        MapPoint res = this.copy();
        for (String name : point.namesAsArray()) {
            res.putValue(name, point.getValue(name));
        }
        return res;
    }
}
