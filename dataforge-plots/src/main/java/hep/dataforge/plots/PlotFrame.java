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
import hep.dataforge.meta.Meta;
import javafx.collections.ObservableList;

import java.io.OutputStream;

/**
 * Набор графиков (plot) в одном окошке (frame) с общими осями.
 *
 * @author Alexander Nozik
 * @param <T>
 */
@ValueDef(name = "title", info = "The title of the plot. By default the name of the Content is taken.")
public interface PlotFrame<T extends Plottable> extends PlotStateListener, Configurable, Wrappable {

    /**
     * Заменить серию с данным именем и перерисовать соответствующий график или
     * добавить новую серию.
     *
     * @param plotable
     */
    void add(T plotable);
    
    /**
     * Add all plottables to the frame
     * @param plottables 
     */
    default void addAll(Iterable<? extends T> plottables){
        for(T pl : plottables){
            add(pl);
        }
    }

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
     * Возвращает загруженную серию, если она есть. Иначе возвращает null
     *
     * @param name
     * @return
     */
    T get(String name);

    /**
     * List of all plottables
     *
     * @return
     */
    ObservableList<T> plottables();

    
    /**
     * Generate a snapshot
     * @param config
     */
    @ValueDef(name = "width", type = "NUMBER", def = "800", info = "The width of the snapshot in pixels")
    @ValueDef(name = "height", type = "NUMBER", def = "600", info = "The height of the snapshot in pixels")
    void snapshot(OutputStream stream, Meta config);
    
}
