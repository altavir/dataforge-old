/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.plots.Plottable;
import hep.dataforge.values.Value;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public class PlottableSet<T extends Plottable> implements  Iterable<T> {

    protected final Map<String, T> map = new LinkedHashMap<>();

    public PlottableSet() {
    }

    public PlottableSet(T... plottables) {
        for (T pl : plottables) {
            map.put(pl.getName(), pl);
        }
    }

    public PlottableSet(Iterable<T> plottables) {
        for (T pl : plottables) {
            map.put(pl.getName(), pl);
        }
    }

    public void addPlottable(T pl) {
        map.put(pl.getName(), pl);
    }

    public void removePlottable(DynamicPlottable pl) {
        map.remove(pl.getName());
    }

    public T getPlottable(String name) {
        return map.get(name);
    }

    public boolean hasPlottable(String name) {
        return map.containsKey(name);
    }

    /**
     * Apply given configuration to each plottable
     *
     * @param config
     */
    public void applyEachConfig(Meta config) {
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
    public void setEachConfigValue(String name, Value value) {
        map.values().stream().forEach((pl) -> {
            pl.getConfig().setValue(name, value);
        });
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