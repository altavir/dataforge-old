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
import hep.dataforge.io.envelopes.EnvelopeWriter;
import hep.dataforge.io.envelopes.WrapperEnvelopeType;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.SimpleConfigurable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static hep.dataforge.plots.wrapper.PlotUnWrapper.PLOT_WRAPPER_TYPE;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractPlotFrame extends SimpleConfigurable implements PlotFrame, Observable {

    private final ObservableMap<String, Plottable> plottables = FXCollections.observableHashMap();


    public AbstractPlotFrame(Configuration configuration) {
        super(configuration);
        setupListener();
    }

    public AbstractPlotFrame() {
        setupListener();
    }

    private void setupListener() {
        plottables.addListener(new MapChangeListener<String, Plottable>() {
            @Override
            public void onChanged(Change<? extends String, ? extends Plottable> change) {
                Plottable removed = change.getValueRemoved();
                Plottable added = change.getValueAdded();

                if (removed != null) {
                    removed.removeListener(AbstractPlotFrame.this);
                    if (removed != added) {
                        updatePlotData(change.getKey());
                    }
                }

                if (added != null) {
                    added.addListener(AbstractPlotFrame.this);
                    updatePlotData(added.getName());
                    updatePlotConfig(added.getName());
                }

            }
        });
    }

    @NotNull
    @Override
    public Iterator<Plottable> iterator() {
        return plottables.values().iterator();
    }

    @Override
    public Optional<Plottable> opt(String name) {
        return Optional.ofNullable(plottables.get(name));
    }

    @Override
    public synchronized void remove(String plotName) {
        plottables.remove(plotName);
    }

    @Override
    public synchronized void clear() {
        plottables.clear();
    }

    @Override
    public synchronized void add(Plottable plottable) {
        plottables.put(plottable.getName(), plottable);
    }

    @Override
    public void setAll(Collection<? extends Plottable> collection) {
        plottables.clear();
        collection.forEach(pl -> add(pl));
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
    public void addListener(InvalidationListener listener) {
        plottables.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        plottables.removeListener(listener);
    }

    @Override
    public Envelope wrap() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EnvelopeWriter writer = new WrapperEnvelopeType().getWriter();
        for (Plottable pl : this) {
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
