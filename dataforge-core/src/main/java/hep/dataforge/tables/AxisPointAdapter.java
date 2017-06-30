/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.tables;

import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NonEmptyMetaMorphException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

import java.util.HashMap;
import java.util.Map;

@ValueDef(name = "label", def = "label", info = "Point label key")
public class AxisPointAdapter implements PointAdapter {

    public static final String VALUE_KEY = "value";
    public static final String ERROR_KEY = "err";
    public static final String LO_KEY = "lo";
    public static final String UP_KEY = "up";

    private Meta meta;
    private final Map<String, String> nameCache = new HashMap<>();

    public AxisPointAdapter() {
        meta = Meta.buildEmpty(DATA_ADAPTER_KEY);
    }

    public AxisPointAdapter(Meta meta) {
        if (meta == null) {
            this.meta = Meta.buildEmpty(DATA_ADAPTER_KEY);
        } else {
            this.meta = meta;
        }
    }

    public AxisPointAdapter(Map<String, String> map) {
        MetaBuilder mb = new MetaBuilder(DATA_ADAPTER_KEY);
        map.entrySet().forEach((entry) -> {
            mb.setValue(entry.getKey(), entry.getValue());
        });
        this.meta = mb.build();
    }

    @Override
    public Meta meta() {
        return meta;
    }

    @Override
    public Value getComponent(Values point, String component) {
        return point.getValue(nameFor(component));
    }

    public Value getFrom(Values point, String component, Object def) {
        return point.getValue(nameFor(component), Value.of(def));
    }

    /**
     * Get node for given axis
     *
     * @param axis
     * @return
     */
    @ValueDef(name = "value", required = true, info = "Axis value key")
    @ValueDef(name = "err", info = "Axis error key")
    @ValueDef(name = "up", info = "Axis upper boundary key")
    @ValueDef(name = "lo", info = "Axis lower boundary key")
    @ValueDef(name = "axisTitle", info = "The title of this axis. By default axis name is used.")
    public Meta getAxisMeta(String axis) {
        return this.meta().getMeta(axis, Meta.empty());
    }

    public Value getValue(Values point, String axis) {
        return getFrom(point, Name.joinString(axis, VALUE_KEY), Value.NULL);
    }

    public Value getError(Values point, String axis) {
        return getFrom(point, Name.joinString(axis, ERROR_KEY), 0);
    }

    /**
     * Upper bound on given axis
     *
     * @param point
     * @return
     */
    public double getUpperBound(Values point, String axis) {
        return point.getDouble(nameFor(Name.joinString(axis, UP_KEY)),
                getValue(point, axis).doubleValue() + getError(point, axis).doubleValue());
    }

    /**
     * Lower bound on given axis
     *
     * @param point
     * @return
     */
    public double getLowerBound(Values point, String axis) {
        return point.getDouble(nameFor(Name.joinString(axis, LO_KEY)),
                getValue(point, axis).doubleValue() - getError(point, axis).doubleValue());
    }

    /**
     * Return the name that should be searched for in the data point for
     * parameter with given name.
     *
     * @param component
     * @return
     */
    protected String nameFor(String component) {
        //caching name to avoid heavy meta request
        if (this.nameCache.containsKey(component)) {
            return nameCache.get(component);
        } else {
            String valueName = meta().getString(component, component);
            nameCache.put(component, valueName);
            return valueName;
        }
    }

    @Override
    public Meta toMeta() {
        return this.meta();
    }

    @Override
    public void fromMeta(Meta meta) {
        if(this.meta != null && !this.meta.isEmpty()){
            throw new NonEmptyMetaMorphException(getClass());
        } else {
            nameCache.clear();
            this.meta = meta;
        }
    }
}
