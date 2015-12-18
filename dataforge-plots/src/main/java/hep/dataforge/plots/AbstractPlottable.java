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
import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author darksnake
 */
public abstract class AbstractPlottable extends SimpleConfigurable implements Plottable, ValueProvider {

    private final String name;
    private final Set<PlotStateListener> listeners = new HashSet<>();

    public AbstractPlottable(String name, Meta config) {
        super(config);
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

    /**
     * Notify all listeners that configuration changed
     *
     * @param config
     */
    @Override
    protected void applyConfig(Meta config) {
        listeners.forEach((l) -> l.notifyConfigurationChanged(getName()));
    }

    /**
     * Notify all listeners that data changed
     */
    public void notifyDataChanged() {
        listeners.forEach((l) -> l.notifyDataChanged(getName()));
    }

    @Override
    public boolean hasValue(String path) {
        return meta().hasValue(path);
    }

    public String getTitle() {
        return getString("title", getName());
    }

    @Override
    public Value getValue(String path) {
        //TODO use descriptor here
        NodeDescriptor descriptor = DescriptorUtils.buildDescriptor(getClass());
        return  DescriptorUtils.extractValue(path, meta(), descriptor);
    }

}
