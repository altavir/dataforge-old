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

import hep.dataforge.fx.FXUtils;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.envelopes.EnvelopeWriter;
import hep.dataforge.io.envelopes.WrapperEnvelopeType;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Named;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

import static hep.dataforge.plots.wrapper.PlotUnWrapper.PLOT_WRAPPER_TYPE;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractPlotFrame extends SimpleConfigurable implements PlotFrame {

    protected ObservableList<Plottable> plottables = FXCollections.observableArrayList();

    //    private String name;
    @Override
    public Plottable get(String name) {
        return plottables.stream().filter(pl -> name.equals(pl.getName())).findFirst().orElse(null);
    }

    @Override
    public ObservableList<Plottable> plottables() {
        return FXCollections.unmodifiableObservableList(plottables);
    }

    @Override
    public synchronized void remove(String plotName) {
        get(plotName).removeListener(this);
        FXUtils.runNow(() -> {
            Plottable pl = get(plotName);
            if (pl != null) {
                pl.removeListener(this);
            }
            plottables.remove(pl);
            updatePlotData(plotName);
        });
    }

    @Override
    public synchronized void clear() {
        plottables.stream().map(Named::getName).collect(Collectors.toList()).stream().forEach(plName -> remove(plName));
    }

    @Override
    public synchronized void add(Plottable plottable) {
        String pName = plottable.getName();
        plottables.add(plottable);
        plottable.addListener(this);
        updatePlotData(pName);
        updatePlotConfig(pName);
    }

    /**
     * Reload data for plottable with given name.
     *
     * @param name
     */
    protected abstract void updatePlotData(String name);

    /**
     * Reload an annotation for given plottable.
     *
     * @param name
     */
    protected abstract void updatePlotConfig(String name);

    @Override
    public void notifyDataChanged(String name) {
        updatePlotData(name);
    }

    @Override
    public void notifyConfigurationChanged(String name) {
        updatePlotConfig(name);
    }

    @Override
    public Envelope wrap() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EnvelopeWriter writer = new WrapperEnvelopeType().getWriter();
        for (Plottable pl : plottables()) {
            try {
                writer.write(baos, pl.wrap());
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write plotable to envelope", ex);
            } catch (UnsupportedOperationException uex) {
                LoggerFactory.getLogger(getClass()).error("Failed to wrap plottable {} becouse wits wrapper is not implemented", pl.getName());
            }
        }

        EnvelopeBuilder builder = new EnvelopeBuilder()
                .putMetaValue(WRAPPED_TYPE_KEY, PLOT_WRAPPER_TYPE)
                .putMetaValue("plotFrameClass", getClass().getName())
                .putMetaNode("plotMeta", meta())
                .setContentType("wrapper")
                .setData(baos.toByteArray());
        return builder.build();
    }

}
