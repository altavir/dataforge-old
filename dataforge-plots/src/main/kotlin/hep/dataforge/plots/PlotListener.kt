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
package hep.dataforge.plots

import hep.dataforge.meta.Laminate
import hep.dataforge.names.Name

/**
 * Listener for plot state changes
 * @author darksnake
 */
interface PlotListener {

    /**
     * Data changed for a specific plot. Data for group could not be changed
     * @param caller the plottable that sent the message
     * @param name the name of added plot relative to callet
     * @param plot the plot itself
     */
    fun dataChanged(caller: Plottable, name: Name, plot: Plot)

    /**
     * Configuration changed for node or plot
     * @param caller the plottable that sent the message
     * @param name full name of  plottable with changed meta relative to caller
     * @param laminate combinded meta of all layers
     */
    fun metaChanged(caller: Plottable, name: Name, laminate: Laminate)

    /**
     * New plot or group added
     * @param caller the plottable that sent the message
     * @param name name of added plottable relative to caller
     * @param plottable the addedd plottable
     */
    fun plotAdded(caller: Plottable, name: Name, plottable: Plottable)

    /**
     * Plot or group removed
     * @param caller the plottable that sent the message
     * @param name name of removed plottable relative to caller
     */
    fun plotRemoved(caller: Plottable, name: Name)
}
