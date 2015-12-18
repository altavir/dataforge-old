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
package hep.dataforge.plots;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Alexander Nozik
 * @param <T>
 */
public abstract class AbstractPlotFrame<T extends Plottable> extends SimpleConfigurable implements PlotFrame<T> {

    protected Map<String, T> plottables = new LinkedHashMap<>();
    private final String name;

    public AbstractPlotFrame(String name, Meta annotation) {
        super(annotation);
        this.name = name;
    }

    @Override
    public T get(String name) {
        return plottables.get(name);
    }

    @Override
    public Collection<? extends T> getAll() {
        return plottables.values();
    }

    @Override
    public void remove(String plotName) {
        plottables.get(plotName).removeListener(this);
        plottables.remove(plotName);
    }

    @Override
    public void add(T plottable) {
        String pName = plottable.getName();
        plottables.put(pName, plottable);
        plottable.addListener(this);
        updatePlotData(pName);
        updatePlotConfig(pName);
    }

    /**
     * Reload data for plottable with given name.
     *
     * @param name
     */
    protected abstract void updatePlotData(String name);

    /**
     * Reload an annotation for given plottable.
     *
     * @param name
     */
    protected abstract void updatePlotConfig(String name);

    @Override
    public String getName() {
        if (name == null || name.isEmpty()) {
            return meta().getString("frame_name");
        } else {
            return name;
        }
    }

//    protected abstract void updatePlot(String name, Descriptor descriptor, Annotation annotation);
//
//    protected abstract void updatePlot(Plottable plottable);
    @Override
    public void notifyDataChanged(String name) {
        updatePlotData(name);
    }

    @Override
    public void notifyConfigurationChanged(String name) {
        updatePlotConfig(name);
    }

}
