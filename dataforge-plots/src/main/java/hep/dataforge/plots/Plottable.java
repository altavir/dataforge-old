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
import static hep.dataforge.fx.MetaTreeItem.NO_CONFIGURATOR_TAG;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.Wrappable;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.names.Named;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.PointAdapter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Единичный набор данных для отрисовки
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "title", info = "The title of series. Could be not unique. By default equals series name.")
@ValueDef(name = "preferedPlotter", def = "jFreeChart",
        info = "A prefered plotting library. It is used if supported by destination PlotFrame.", tags = {NO_CONFIGURATOR_TAG})
@ValueDef(name = "visble", def = "true", type = "BOOLEAN", info = "The current visiblity of this plottable")
@AnonimousNotAlowed
public interface Plottable<T extends PointAdapter> extends Named, Annotated, Configurable, Wrappable {

    /**
     * Stream of data for plotting with default configuration
     *
     * @return
     */
    default Stream<DataPoint> dataStream() {
        return dataStream(Meta.empty());
    }

    /**
     * Stream of data for plotting using given configuration (range, number of
     * points, etc.)
     *
     * @param dataConfiguration
     * @return
     */
    Stream<DataPoint> dataStream(Meta dataConfiguration);

    default List<DataPoint> data() {
        return data(Meta.empty());
    }

    default List<DataPoint> data(Meta dataConfiguration) {
        return dataStream(dataConfiguration).collect(Collectors.toList());
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
    T adapter();

    /**
     * Set custom adapter for this plottable. This method should be used with
     * caution since it does not guarantee wrapping/unwrapping invariance
     *
     * @param adapter
     */
    void setAdapter(T adapter);

    /**
     * Set adapter generated from given meta
     *
     * @param adapterMeta
     */
    void setAdapter(Meta adapterMeta);

    @Override
    public default Envelope wrap() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
