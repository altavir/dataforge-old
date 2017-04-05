/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.plots.Plottable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public class PlottableGroup<T extends Plottable> implements Iterable<T> {

    //TODO replace by obsevable collection
    protected final Map<String, T> map = new LinkedHashMap<>();

    public PlottableGroup() {
    }

    public PlottableGroup(T... plottables) {
        for (T pl : plottables) {
            map.put(pl.getName(), pl);
        }
    }

    public PlottableGroup(Iterable<T> plottables) {
        for (T pl : plottables) {
            map.put(pl.getName(), pl);
        }
    }

    public void add(T pl) {
        map.put(pl.getName(), pl);
    }

    public void remove(String name) {
        map.remove(name);
    }

    public T get(String name) {
        return map.get(name);
    }

    public boolean has(String name) {
        return map.containsKey(name);
    }

    /**
     * Apply given configuration to each plottable
     *
     * @param config
     */
    public void apply(Meta config) {
        map.values().stream().forEach((pl) -> {
            pl.configure(config);
        });
    }

    /**
     * Set configuration value for each plottable
     *
     * @param name
     * @param value
     */
    public void setValue(String name, Object value) {
        map.values().stream().forEach((pl) -> {
            pl.getConfig().setValue(name, value);
        });
    }

    /**
     * Apply configuration to plottables considering each plottable described
     * with appropriate {@code plot} node.
     * <p>
     * <p>
     * A node marked {@code eachPlot} is applied to each plottable previously to individual configurations.
     * </p>
     *
     * @param config
     */
    public void applyConfig(Meta config) {
        if (config.hasMeta("eachPlot")) {
            apply(config.getMeta("eachPlot"));
        }
        map.values().stream().forEach((pl) -> {
            Meta m = MetaUtils.findNodeByValue(config, "plot", "name", pl.getName());
            if (m != null) {
                pl.configure(m);
            }
        });
    }


    @Override
    public Iterator<T> iterator() {
        return this.map.values().iterator();
    }
}
