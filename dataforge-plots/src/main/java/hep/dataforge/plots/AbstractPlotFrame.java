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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractPlotFrame extends SimpleConfigurable implements PlotFrame, PlotListener {

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

    /**
     * Reload data for plottable with given name.
     *
     * @param name
     */
    protected abstract void updatePlotData(Name name, @NotNull Plot plot);

    /**
     * Reload an annotation for given plottable.
     *
     * @param name
     */
    protected abstract void updatePlotConfig(Name name, Laminate config);


    @Override
    public void dataChanged(Name name, Plot plot) {
        updatePlotData(name, plot);
    }

    @Override
    public void metaChanged(Name name, Plottable plottable, Laminate laminate) {
        if (plottable instanceof Plot) {
            updatePlotConfig(name, laminate);
        }
    }

    @Override
    public void plotAdded(Name name, Plottable plottable) {
        if (plottable instanceof Plot) {
            updatePlotData(name, (Plot) plottable);
        }
    }

}
