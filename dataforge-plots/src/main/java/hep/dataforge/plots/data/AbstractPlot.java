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

import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Name;
import hep.dataforge.plots.Plot;
import hep.dataforge.plots.PlotListener;
import hep.dataforge.tables.Adapters;
import hep.dataforge.tables.ValuesAdapter;
import hep.dataforge.utils.ReferenceRegistry;
import org.jetbrains.annotations.NotNull;

import static hep.dataforge.tables.ValuesAdapter.ADAPTER_KEY;

/**
 * @author darksnake
 */
public abstract class AbstractPlot extends SimpleConfigurable implements Plot {

    //public static final String PLOTTABLE_WRAPPER_TYPE = "plottable";

    private final Name name;
    private ReferenceRegistry<PlotListener> listeners = new ReferenceRegistry<>();
    private ValuesAdapter adapter;

    public AbstractPlot(Name name) {
        this.name = name;
    }

    public AbstractPlot(@NotNull String name) {
        this.name = Name.ofSingle(name);
    }

    @Override
    public void addListener(@NotNull PlotListener listener) {
        listeners.add(listener,true);
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public void removeListener(@NotNull PlotListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public ValuesAdapter getAdapter() {
        //If adapter is not defined, creating new adapter.
        if (adapter == null) {
            adapter = Adapters.buildAdapter(getConfig().getMeta(ADAPTER_KEY, Meta.empty()));
        }
        return adapter;
    }

    /**
     * Notify all listeners that configuration changed
     *
     * @param config
     */
    @Override
    protected synchronized void applyConfig(Meta config) {
        listeners.forEach((l) -> l.metaChanged(name, this, new Laminate(getConfig())));

//        //invalidate adapter
//        if (config.hasMeta(ADAPTER_KEY)) {
//            adapter = null;
//        }
    }

    /**
     * Notify all listeners that data changed
     */
    public synchronized void notifyDataChanged() {
        listeners.forEach((l) -> l.dataChanged(name, this));
    }


    public void setAdapter(ValuesAdapter adapter) {
        this.configureNode(ADAPTER_KEY, adapter.toMeta());
        this.adapter = adapter;
    }

}
