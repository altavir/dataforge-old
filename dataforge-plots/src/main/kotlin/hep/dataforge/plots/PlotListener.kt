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
     * @param name
     * @param plot
     */
    fun dataChanged(name: Name, plot: Plot)

    /**
     * Configuration changed for node or plot
     * @param name full name of calling plottable
     * @param plottable a caller object
     * @param laminate combinded meta of all layers
     */
    fun metaChanged(name: Name, plottable: Plottable, laminate: Laminate)

    /**
     * Ne plot or node added
     * @param name
     * @param plottable
     */
    fun plotAdded(name: Name, plottable: Plottable)

    /**
     * plot or node removed
     * @param name
     */
    fun plotRemoved(name: Name)
}
