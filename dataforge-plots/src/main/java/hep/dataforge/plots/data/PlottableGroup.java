/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Name;
import hep.dataforge.plots.Plot;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
@Deprecated
public class PlottableGroup<T extends Plot> extends SimpleConfigurable implements Iterable<T>{

    //TODO replace by obsevable collection
    protected final Map<Name, T> map = new LinkedHashMap<>();

    public PlottableGroup() {
    }

    @SafeVarargs
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
     * Set configuration value for each plottable
     *
     * @param name
     * @param value
     */
    public void setValue(String name, Object value) {
        map.values().forEach((pl) -> {
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
    protected void applyConfig(Meta config) {
        map.values().forEach((pl) -> {
            pl.configure(config);
        });
        map.values().forEach(pl -> MetaUtils.findNodeByValue(config, "plot", "name", pl.getName()).ifPresent(pl::configure));
    }


    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.map.values().iterator();
    }
}
