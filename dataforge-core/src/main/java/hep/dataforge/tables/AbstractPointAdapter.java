/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.tables;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPointAdapter implements PointAdapter {

    private final Meta meta;
    private final Map<String, String> nameCache = new HashMap<>();

    public AbstractPointAdapter() {
        meta = Meta.buildEmpty(DATA_ADAPTER_ANNOTATION_NAME);
    }

    public AbstractPointAdapter(Meta meta) {
        if (meta == null) {
            this.meta = Meta.buildEmpty(DATA_ADAPTER_ANNOTATION_NAME);
        } else {
            this.meta = meta;
        }
    }

    /**
     *
     * @param map
     */
    public AbstractPointAdapter(Map<String, String> map) {
        MetaBuilder mb = new MetaBuilder(DATA_ADAPTER_ANNOTATION_NAME);
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
