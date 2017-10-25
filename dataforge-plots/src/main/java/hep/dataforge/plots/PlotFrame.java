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
import hep.dataforge.io.envelopes.*;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Meta;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.Optional;

import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;
import static hep.dataforge.values.ValueType.NUMBER;

/**
 * Набор графиков (plot) в одном окошке (frame) с общими осями.
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "title", info = "The title of the plot. By default the name of the Content is taken.")
public interface PlotFrame extends PlotStateListener, Configurable, Serializable {

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
    void add(Plottable plotable);

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
    @ValueDef(name = "width", type = {NUMBER}, def = "800", info = "The width of the snapshot in pixels")
    @ValueDef(name = "height", type = {NUMBER}, def = "600", info = "The height of the snapshot in pixels")
    default void save(OutputStream stream, Meta config) {
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
            super(envelope.meta(), envelope.getData());
        }

        private Object readResolve() throws ObjectStreamException {
            return wrapper.unWrap(this);
        }
    }


    class Wrapper implements hep.dataforge.io.envelopes.Wrapper<PlotFrame> {

        public static final String PLOT_FRAME_WRAPPER_TYPE = "df.plots.frame";

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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            EnvelopeWriter writer = DefaultEnvelopeType.instance.getWriter();

            try {
                writer.write(baos, new PlotGroup.Wrapper().wrap(frame.getPlots()));
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write plottable to envelope", ex);
            }

            EnvelopeBuilder builder = new EnvelopeBuilder()
                    .putMetaValue(WRAPPER_KEY, PLOT_FRAME_WRAPPER_TYPE)
                    .putMetaValue("plotFrameClass", frame.getClass().getName())
                    .putMetaNode(DEFAULT_META_NAME, frame.getConfig())
                    .setContentType("wrapper")
                    //.putMetaNode("root.meta",frame.ro)
                    .setData(baos.toByteArray());
            return builder.build();
        }

        @Override
        public PlotFrame unWrap(Envelope envelope) {
            String plotFrameClassName = envelope.meta().getString("plotFrameClass", "hep.dataforge.plots.JFreeChartFrame");
            Meta plotMeta = envelope.meta().getMetaOrEmpty(DEFAULT_META_NAME);

            EnvelopeType internalEnvelopeType = EnvelopeType.resolve(envelope.meta().getString("envelopeType", "default"));
            try {
                PlotFrame frame = (PlotFrame) Class.forName(plotFrameClassName).getConstructor().newInstance();
                frame.configure(plotMeta);
                //Buffering stream to avoid rebufferization
                BufferedInputStream dataStream = new BufferedInputStream(envelope.getData().getStream());
                Plot.Wrapper unwrapper = new Plot.Wrapper();

                while (dataStream.available() > 0) {
                    try {
                        Plot pl = unwrapper.unWrap(internalEnvelopeType.getReader().read(dataStream));
                        frame.add(pl);
                    } catch (Exception ex) {
                        LoggerFactory.getLogger(getClass()).error("Failed to unwrap plottable", ex);
                    }

                }

                return frame;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

    }

}
