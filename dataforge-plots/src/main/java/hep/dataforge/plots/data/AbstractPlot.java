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
import hep.dataforge.tables.ValuesAdapter;
import hep.dataforge.utils.ReferenceRegistry;
import org.jetbrains.annotations.NotNull;

import static hep.dataforge.tables.ValuesAdapter.ADAPTER_KEY;

/**
 * @author darksnake
 */
public abstract class AbstractPlot<T extends ValuesAdapter> extends SimpleConfigurable implements Plot {

    public static final String PLOTTABLE_WRAPPER_TYPE = "plottable";

    private final String name;
    private ReferenceRegistry<PlotListener> listeners = new ReferenceRegistry<>();
    private T adapter;

//    public AbstractPlot(String name, @NotNull T adapter) {
//        this(name);
//        setAdapter(adapter);
//    }

    public AbstractPlot(@NotNull String name) {
        this.name = name.replace(".", "_");
    }

    @Override
    public void addListener(@NotNull PlotListener listener) {
        getListeners().add(listener);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void removeListener(@NotNull PlotListener listener) {
        this.getListeners().remove(listener);
    }

    @Override
    public T getAdapter() {
        //If adapter is not defined, creating new adapter.
        if (adapter == null) {
            adapter = buildAdapter(meta().getMeta(ADAPTER_KEY, Meta.empty()));
        }
        return adapter;
    }

    protected abstract T buildAdapter(Meta adapterMeta);

    /**
     * Notify all listeners that configuration changed
     *
     * @param config
     */
    @Override
    protected synchronized void applyConfig(Meta config) {
        getListeners().forEach((l) -> l.metaChanged(Name.of(getName()), this, new Laminate(meta())));

        //invalidate adapter
        if (config.hasMeta(ADAPTER_KEY)) {
            adapter = null;
        }
    }

    /**
     * Notify all listeners that data changed
     */
    public synchronized void notifyDataChanged() {
        getListeners().forEach((l) -> l.dataChanged(Name.of(getName()), this));
    }

    public String getTitle() {
        return meta().getString("title", getName());
    }

    /**
     * @return the listeners
     */
    private ReferenceRegistry<PlotListener> getListeners() {
        return listeners;
    }

    public void setAdapter(T adapter) {
        this.adapter = adapter;
        this.configureNode(ADAPTER_KEY, adapter.meta());
    }

}
