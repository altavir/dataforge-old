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

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.tables.PointAdapter;
import hep.dataforge.utils.ReferenceRegistry;

/**
 *
 * @author darksnake
 */
public abstract class AbstractPlottable<T extends PointAdapter> extends SimpleConfigurable implements Plottable<T> {
    
    public static final String ADAPTER_KEY = "adapter";

    private final String name;
    private final ReferenceRegistry<PlotStateListener> listeners = new ReferenceRegistry<>();
    private T adapter;

//    public AbstractPlottable(String name, Meta metaBase, Meta config) {
//        this.name = name;
//        if (metaBase != null) {
//            super.setMetaBase(metaBase);
//        }
//        if (config != null) {
//            super.configure(config);
//        }
//    }

    public AbstractPlottable(String name, @NonNull T adapter) {
        this(name);
        setAdapter(adapter);
    }
    
    public AbstractPlottable(@NonNull String name) {
        this.name = name;
    }

    @Override
    public void addListener(PlotStateListener listener) {
        listeners.add(listener);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void removeListener(PlotStateListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public T adapter() {
        if(adapter == null){
            return defaultAdapter();
        } else {
            return adapter;
        }
    }

    @Override
    public final void setAdapter(T adapter) {
        this.adapter = adapter;
        //Silently update meta to include adapter
        this.getConfig().putNode(ADAPTER_KEY, adapter.meta(), false);
    }

    @Override
    public final void setAdapter(Meta adapterMeta) {
        setAdapter(buildAdapter(adapterMeta));
    }
    
    protected abstract T buildAdapter(Meta adapterMeta);
    
    protected abstract T defaultAdapter();

    /**
     * Notify all listeners that configuration changed
     *
     * @param config
     */
    @Override
    protected void applyConfig(Meta config) {
        listeners.forEach((l) -> l.notifyConfigurationChanged(getName()));
        //If adapter is not defined, creating new adapter.
        if(this.adapter == null && config.hasNode(ADAPTER_KEY)){
            setAdapter(config.getNode(ADAPTER_KEY));
        }
    }

    /**
     * Notify all listeners that data changed
     */
    public void notifyDataChanged() {
        listeners.forEach((l) -> l.notifyDataChanged(getName()));
    }

    public String getTitle() {
        return meta().getString("title", getName());
    }

}
