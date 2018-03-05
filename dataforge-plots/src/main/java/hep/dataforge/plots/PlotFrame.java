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
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.envelopes.SimpleEnvelope;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Meta;

import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

/**
 * Набор графиков (plot) в одном окошке (frame) с общими осями.
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "title", info = "The title of the plot. By default the name of the Content is taken.")
public interface PlotFrame extends Configurable, Serializable {

    Wrapper wrapper = new Wrapper();

    /**
     * Get root plot node
     *
     * @return
     */
    PlotGroup getPlots();

    /**
     * Add or replace registered plottable
     *
     * @param plotable
     */
    default void add(Plottable plotable){
        getPlots().add(plotable);
    }

    /**
     * Add (replace) all plottables to the frame
     *
     * @param plottables
     */
    default void addAll(Iterable<? extends Plottable> plottables) {
        for (Plottable pl : plottables) {
            add(pl);
        }
    }

    /**
     * Update all plottables. Remove the ones not present in a new set
     *
     * @param plottables
     */
    default void setAll(Collection<? extends Plottable> plottables) {
        clear();
        plottables.forEach(this::add);
    }

    /**
     * Remove plottable with given name
     *
     * @param plotName
     */
    default void remove(String plotName) {
        getPlots().remove(plotName);
    }

    /**
     * Remove all plottables
     */
    default void clear() {
        getPlots().clear();
    }


    /**
     * Opt the plottable with the given name
     *
     * @param name
     * @return
     */
    Optional<Plot> opt(String name);

    default Plot get(String name) {
        return opt(name).orElseThrow(() -> new NameNotFoundException(name));
    }

    /**
     * Save plot as image
     *
     * @param stream
     * @param config
     */
    default void asImage(OutputStream stream, Meta config) {
        throw new UnsupportedOperationException();
    }

//    default Object writeReplace() throws ObjectStreamException {
//        return new PlotFrameEnvelope(wrapper.wrap(this));
//    }

    /**
     * Use exclusively for plot frame serialization
     */
    class PlotFrameEnvelope extends SimpleEnvelope {

        public PlotFrameEnvelope() {
        }

        public PlotFrameEnvelope(Envelope envelope) {
            super(envelope.getMeta(), envelope.getData());
        }

        private Object readResolve() throws ObjectStreamException {
            return wrapper.unWrap(this);
        }
    }


    class Wrapper implements hep.dataforge.io.envelopes.Wrapper<PlotFrame> {
        public static final String PLOT_FRAME_WRAPPER_TYPE = "df.plots.frame";
        public static final String PLOT_FRAME_CLASS_KEY = "frame.class";
        public static final String PLOT_FRAME_META_KEY = "frame.meta";

        @Override
        public String getName() {
            return PLOT_FRAME_WRAPPER_TYPE;
        }

        @Override
        public Class<PlotFrame> getType() {
            return PlotFrame.class;
        }

        @Override
        public Envelope wrap(PlotFrame frame) {
            Envelope rootEnv = PlotGroup.WRAPPER.wrap(frame.getPlots());

            EnvelopeBuilder builder = new EnvelopeBuilder()
                    .setMeta(rootEnv.getMeta())
                    .setData(rootEnv.getData())
                    .setContentType("wrapper")
                    .setMetaValue(WRAPPER_TYPE_KEY, PLOT_FRAME_WRAPPER_TYPE)
                    .setMetaValue(PLOT_FRAME_CLASS_KEY, frame.getClass().getName())
                    .putMetaNode(PLOT_FRAME_META_KEY, frame.getConfig());
            return builder.build();
        }

        @Override
        public PlotFrame unWrap(Envelope envelope) {
            PlotGroup root = PlotGroup.WRAPPER.unWrap(envelope);

            String plotFrameClassName = envelope.getMeta().getString(PLOT_FRAME_CLASS_KEY, "hep.dataforge.plots.JFreeChartFrame");
            Meta plotMeta = envelope.getMeta().getMetaOrEmpty(PLOT_FRAME_META_KEY);

            try {
                PlotFrame frame = (PlotFrame) Class.forName(plotFrameClassName).getConstructor().newInstance();
                frame.configure(plotMeta);
                frame.addAll(root);
                frame.getPlots().configure(root.getConfig());

                return frame;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

    }

}
