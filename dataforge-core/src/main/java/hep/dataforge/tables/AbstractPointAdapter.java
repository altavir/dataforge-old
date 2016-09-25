/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.tables;

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;
import hep.dataforge.values.Value;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPointAdapter implements PointAdapter {

    public static final String VALUE_KEY = "value";
    public static final String ERROR_KEY = "err";
    public static final String LO_KEY = "lo";
    public static final String UP_KEY = "up";

    private final Meta meta;
    private final Map<String, String> nameCache = new HashMap<>();

    public AbstractPointAdapter() {
        meta = Meta.buildEmpty(DATA_ADAPTER_KEY);
    }

    public AbstractPointAdapter(Meta meta) {
        if (meta == null) {
            this.meta = Meta.buildEmpty(DATA_ADAPTER_KEY);
        } else {
            this.meta = meta;
        }
    }

    public AbstractPointAdapter(Map<String, String> map) {
        MetaBuilder mb = new MetaBuilder(DATA_ADAPTER_KEY);
        map.entrySet().stream().forEach((entry) -> {
            mb.setValue(entry.getKey(), entry.getValue());
        });
        this.meta = mb.build();
    }

    @Override
    public Meta meta() {
        return meta;
    }

    @Override
    public Value getFrom(DataPoint point, String component) {
        return point.getValue(getValueName(component));
    }

    public Value getFrom(DataPoint point, String component, Object def) {
        return point.getValue(getValueName(component), Value.of(def));
    }

    /**
     * Get node for given axis
     *
     * @param axis
     * @return
     */
    @ValueDef(name = "value", def = "value", required = true, info = "value key")
    @ValueDef(name = "err", def = "err", info = "error key")
    @ValueDef(name = "up", def = "up", info = "upper boundary key")
    @ValueDef(name = "lo", def = "lo", info = "lower boundary key")
    @ValueDef(name = "label", def = "label", info = "point label key")
    @ValueDef(name = "axisTitle", info = "The title of this axis. By default axis name is used.")
    public Meta getAxisMeta(String axis) {
        return this.meta().getNode(axis, Meta.empty());
    }

    public Value getValue(DataPoint point, String axis) {
        return getFrom(point, Name.joinString(axis, VALUE_KEY), Value.NULL);
    }

    public Value getError(DataPoint point, String axis) {
        return getFrom(point, Name.joinString(axis, ERROR_KEY), 0);
    }

    /**
     * Upper bound on given axis
     *
     * @param point
     * @return
     */
    public double getUpperBound(DataPoint point, String axis) {
        return point.getDouble(getValueName(Name.joinString(axis, UP_KEY)),
                getValue(point, axis).doubleValue() + getError(point, axis).doubleValue());
    }

    /**
     * Lower bound on given axis
     *
     * @param point
     * @return
     */
    public double getLowerBound(DataPoint point, String axis) {
        return point.getDouble(getValueName(Name.joinString(axis, LO_KEY)),
                getValue(point, axis).doubleValue() - getError(point, axis).doubleValue());
    }

    /**
     * Return the name that should be searched for in the data point for
     * parameter with given name.
     *
     * @param component
     * @return
     */
    protected String getValueName(String component) {
        //caching name to avoid heavy meta request
        if (this.nameCache.containsKey(component)) {
            return nameCache.get(component);
        } else {
            String valueName = meta().getString(component, component);
            nameCache.put(component, valueName);
            return valueName;
        }
    }

}
