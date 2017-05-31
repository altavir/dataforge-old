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

import hep.dataforge.description.Described;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.io.envelopes.Wrappable;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.names.Named;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.PointAdapter;
import hep.dataforge.values.Value;

import java.util.List;

import static hep.dataforge.values.ValueType.BOOLEAN;
import static hep.dataforge.values.ValueType.NUMBER;

/**
 * Единичный набор данных для отрисовки
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "title", info = "The title of series. Could be not unique. By default equals series name.")
//@ValueDef(name = "preferedPlotter", def = "jFreeChart",
//        info = "A prefered plotting library. It is used if supported by destination PlotFrame.", tags = {NO_CONFIGURATOR_TAG})
@ValueDef(name = "visible", def = "true", type = {BOOLEAN}, info = "The current visibility of this plottable")
@AnonimousNotAlowed
public interface Plottable extends Named, Metoid, Configurable, Wrappable, Described {

    /**
     * Get immutable list of data data according to query
     *
     * @param query
     * @return
     */
    @NodeDef(name = "xRange", info = "X filter")
    @ValueDef(name = "xRange.from", type = {NUMBER}, info = "X range from")
    @ValueDef(name = "xRange.to", type = {NUMBER}, info = "X range to")
    @ValueDef(name = "numPoints", type = {NUMBER}, info = "A required number of visible points. The real number could differ from requested one.")
    List<DataPoint> getData(Meta query);

    default List<DataPoint> getData() {
        return getData(Meta.empty());
    }

    /**
     * Add plottable state listener
     *
     * @param listener
     */
    void addListener(PlotStateListener listener);

    /**
     * Remove plottable state listener
     *
     * @param listener
     */
    void removeListener(PlotStateListener listener);

    /**
     * Get current adapter for this plottable
     *
     * @return
     */
    PointAdapter getAdapter();

    default Value getComponent(int index, String component) {
        return getAdapter().getComponent(getData().get(index), component);
    }

}
