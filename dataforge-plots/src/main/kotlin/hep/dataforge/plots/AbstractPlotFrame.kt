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


    /**
     * recursively apply some action to all plottables in hierarchy starting at root
     */
    private fun recursiveApply(root: Name, action: (path: Name, plot: Plottable) -> Unit) {
        plots[root]?.let {
            action.invoke(root, it)
            if (it is PlotGroup) {
                it.forEach {
                    recursiveApply(root + it.name, action)
                }
            }
        }
    }

    private fun resolveMeta(root: Name, path:Name, meta:La): Laminate {

    }

    override fun dataChanged(caller: Plottable, path: Name) {
        recursiveApply(path) { name, plot ->
            (plot as? Plot)?.let { updatePlotData(name, it) }
        }
    }

    override fun metaChanged(caller: Plottable, path: Name) {
        recursiveApply(path) { name, plot ->
            (plot as? Plot)?.let { updatePlotConfig(name, resolveMeta(name)) }
        }
    }

}
