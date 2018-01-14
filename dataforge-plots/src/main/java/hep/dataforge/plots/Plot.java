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

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.plots.data.DataPlot;
import hep.dataforge.tables.ListOfPoints;
import hep.dataforge.tables.ValuesAdapter;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;

/**
 * Единичный набор данных для отрисовки
 *
 * @author Alexander Nozik
 */
//@ValueDef(name = "preferedPlotter", def = "jFreeChart",
//        info = "A prefered plotting library. It is used if supported by destination PlotFrame.", tags = {NO_CONFIGURATOR_TAG})
@AnonymousNotAlowed
public interface Plot extends Plottable {

    /**
     * Get immutable list of data data according to query
     *
     * @param query
     * @return
     */
    List<Values> getData(Meta query);

    /**
     * Get the whole data set without limitations
     *
     * @return
     */
    default List<Values> getData() {
        return getData(Meta.empty());
    }

    /**
     * Get current adapter for this plottable
     *
     * @return
     */
    ValuesAdapter getAdapter();

    default Value getComponent(int index, String component) {
        return getAdapter().getComponent(getData().get(index), component);
    }

    class Wrapper implements hep.dataforge.io.envelopes.Wrapper<Plot> {
        public static final String PLOT_WRAPPER_TYPE = "df.plots.plot";

        @Override
        public String getName() {
            return PLOT_WRAPPER_TYPE;
        }

        @Override
        public Class<Plot> getType() {
            return Plot.class;
        }

        @Override
        public Envelope wrap(Plot plot) {
            EnvelopeBuilder builder = new EnvelopeBuilder()
                    .putMetaValue(WRAPPER_TYPE_KEY, PLOT_WRAPPER_TYPE)
                    .putMetaValue(WRAPPER_CLASS_KEY, getClass().getName())
                    .putMetaValue("name", plot.getName())
                    //.putMetaNode("descriptor", plot.getDescriptor().toMeta())
                    .putMetaNode(DEFAULT_META_NAME, plot.getConfig())
                    .setContentType("wrapper");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
                os.writeObject(new ListOfPoints(plot.getData()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            builder.setData(baos.toByteArray());
            return builder;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Plot unWrap(Envelope envelope) {
            try {
                Meta meta = envelope.getMeta().getMetaOrEmpty(DEFAULT_META_NAME);
                String name = envelope.getMeta().getString("name");

                ListOfPoints data = (ListOfPoints) new ObjectInputStream(envelope.getData().getStream()).readObject();

                //Restore always as plottableData
                DataPlot pl = new DataPlot(name, meta);
                pl.fillData(data);
                return pl;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to read Plot", ex);
            }
        }

    }

}
