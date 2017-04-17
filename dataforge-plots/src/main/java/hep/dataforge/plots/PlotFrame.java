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

import hep.dataforge.description.ValueDef;
import hep.dataforge.io.envelopes.Wrappable;
import hep.dataforge.meta.Configurable;

import java.util.Collection;
import java.util.Optional;

/**
 * Набор графиков (plot) в одном окошке (frame) с общими осями.
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "title", info = "The title of the plot. By default the name of the Content is taken.")
public interface PlotFrame extends PlotStateListener, Configurable, Wrappable, Iterable<Plottable> {

    /**
     * Add or replace registered plottable
     * @param plotable
     */
    void add(Plottable plotable);
    
    /**
     * Add (replace) all plottables to the frame
     * @param plottables 
     */
    default void addAll(Iterable<? extends Plottable> plottables){
        for(Plottable pl : plottables){
            add(pl);
        }
    }

    /**
     * Update all plottables. Remove the ones not present in a new set
     * @param plottables
     */
    void setAll(Collection<? extends Plottable> plottables);

    /**
     * Remove plottable with given name
     *
     * @param plotName
     */
    void remove(String plotName);
    
    /**
     * Remove all plottables
     */
    void clear();

    /**
     * Opt the plottable with the given name
     * @param name
     * @return
     */
    Optional<Plottable> opt(String name);

    default Plottable get(String name){
        return opt(name).get();
    }

}
