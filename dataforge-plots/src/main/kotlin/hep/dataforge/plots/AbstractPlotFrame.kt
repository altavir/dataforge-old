/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.plots

import hep.dataforge.meta.Laminate
import hep.dataforge.meta.SimpleConfigurable
import hep.dataforge.names.Name

/**
 * @author Alexander Nozik
 */
abstract class AbstractPlotFrame : SimpleConfigurable(), PlotFrame, PlotListener {

    final override val plots by lazy {
        PlotGroup("").apply { addListener(this@AbstractPlotFrame, false) }
    }

    override fun get(name: String): Plottable? {
        return plots[name]
    }

    /**
     * Reload data for plottable with given name.
     *
     * @param name
     */
    protected abstract fun updatePlotData(name: Name, plot: Plot)

    /**
     * Reload an annotation for given plottable.
     *
     * @param name
     */
    protected abstract fun updatePlotConfig(name: Name, config: Laminate)


    override fun dataChanged(name: Name, plot: Plot) {
        updatePlotData(name, plot)
    }

    override fun metaChanged(name: Name, plottable: Plottable, laminate: Laminate) {
        if (plottable is Plot) {
            updatePlotConfig(name, laminate)
        }
    }

    override fun plotAdded(name: Name, plottable: Plottable) {
        if (plottable is Plot) {
            updatePlotData(name, plottable)
        }
    }

}
