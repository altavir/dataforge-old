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

import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Name;

import java.util.Optional;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractPlotFrame extends SimpleConfigurable implements PlotFrame {

    private PlotGroup root = new PlotGroup("");

    {
        root.addListener(this);
    }

    public AbstractPlotFrame(Configuration configuration) {
        super(configuration);
    }

    public AbstractPlotFrame() {
    }


    public PlotGroup getPlots() {
        return root;
    }

    @Override
    public Optional<Plot> opt(String name) {
        return root.opt(name).flatMap(it -> {
            if (it instanceof Plot) {
                return Optional.of((Plot) it);
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public synchronized void add(Plottable plot) {
        root.add(plot);
    }

    /**
     * Reload data for plottable with given name.
     *
     * @param name
     */
    protected abstract void updatePlotData(String name, Plot plot);

    /**
     * Reload an annotation for given plottable.
     *
     * @param name
     */
    protected abstract void updatePlotConfig(String name, Laminate config);

    @Override
    public void notifyDataChanged(String name) {
        opt(name).ifPresent(plt -> updatePlotData(name, plt));
    }

    @Override
    public void notifyConfigurationChanged(String name) {
        Plottable plt = getPlots().

                root.getPlotMeta(Name.of(name)).ifPresent(laminate -> updatePlotConfig(name, laminate));
    }

    @Override
    public void notifyGroupChanged(String name) {
        Optional<Plot> plt = opt(name);
        if (plt.isPresent()) {
            updatePlotData(name, plt.get());
            notifyConfigurationChanged(name);
        } else {
            root.remove(name);
        }
    }
}
