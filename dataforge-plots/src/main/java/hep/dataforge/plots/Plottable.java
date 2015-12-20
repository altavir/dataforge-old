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

import hep.dataforge.meta.Configurable;
import hep.dataforge.content.AnonimousNotAlowed;
import hep.dataforge.content.Named;
import hep.dataforge.data.DataPoint;
import hep.dataforge.description.ValueDef;
import java.util.Collection;

/**
 * Единичный набор данных для отрисовки
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "title", info = "The title of series. Could be not unique. By default equals series name.")
@ValueDef(name = "preferedPlotter", def = "jFreeChart", info = "A prefered plotting library. It is used if supported by destination PlotFrame.")
@AnonimousNotAlowed
public interface Plottable extends Named, Configurable{

    /**
     * Данные для рисования.
     *
     * @return
     */
    Collection<DataPoint> plotData();

    /**
     * Add plottable state listener
     * @param listener 
     */
    void addListener(PlotStateListener listener);

    /**
     * Remove plottable state listener
     * @param listener 
     */
    void removeListener(PlotStateListener listener);

}