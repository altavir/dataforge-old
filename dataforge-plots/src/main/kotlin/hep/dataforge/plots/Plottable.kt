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

import hep.dataforge.description.Described
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.meta.Configurable
import hep.dataforge.names.Name
import hep.dataforge.values.ValueType

@ValueDefs(
        ValueDef(name = "title", info = "The title of series. Could be not unique. By default equals series name."),
        ValueDef(name = "visible", def = "true", type = arrayOf(ValueType.BOOLEAN), info = "The current visibility of this plottable")
)
interface Plottable : Configurable, Described {

    val name: Name

    val title: String
        get() = config.getString("title", name.toUnescaped())

    /**
     * Add plottable state listener
     *
     * @param listener
     */
    fun addListener(listener: PlotListener)

    /**
     * Remove plottable state listener
     *
     * @param listener
     */
    fun removeListener(listener: PlotListener)

    companion object {
        const val PLOT_TYPE = "hep.dataforge.plot"
    }
}
