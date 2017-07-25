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
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static hep.dataforge.plots.wrapper.PlotUnWrapper.PLOT_WRAPPER_TYPE;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractPlotFrame extends SimpleConfigurable implements PlotFrame {

    private final Map<String, Plottable> plottables = new LinkedHashMap<>();

    private ListBinding<Plottable> list = new ListBinding<Plottable>() {
        @Override
        protected ObservableList<Plottable> computeValue() {
            return FXCollections.observableArrayList(plottables.values());
        }
    };


    public AbstractPlotFrame(Configuration configuration) {
        super(configuration);
        //setupListener();
    }

    public AbstractPlotFrame() {
        //setupListener();
    }

    @Override
    public Iterator<Plottable> iterator() {
        return plottables.values().iterator();
    }

    @Override
    public ObservableList<Plottable> plottables() {
        return list;
    }

    @Override
    public Optional<Plottable> opt(String name) {
        return Optional.ofNullable(plottables.get(name));
    }

    @Override
    public synchronized void remove(String plotName) {
        Plottable removed = plottables.remove(plotName);
        if (removed != null) {
            removed.removeListener(this);
            updatePlotData(plotName);
            list.invalidate();
        }
    }

    @Override
    public synchronized void clear() {
        Collection<String> names = plottables.keySet();
        this.forEach(plottable -> plottable.removeListener(this));
        plottables.clear();
        names.forEach(this::updatePlotData);
        list.invalidate();
    }

    @Override
    public synchronized void add(Plottable plottable) {
        Plottable prev = plottables.put(plottable.getName(), plottable);
        if (prev != plottable) {
            plottable.addListener(AbstractPlotFrame.this);
            updatePlotData(plottable.getName());
            updatePlotConfig(plottable.getName());
            list.invalidate();
        }
    }

    @Override
    public synchronized void setAll(Collection<? extends Plottable> collection) {
        Set<String> invalidateNames = new HashSet<>();
        plottables.values().forEach(plottable -> {
            plottable.removeListener(AbstractPlotFrame.this);
            invalidateNames.add(plottable.getName());
        });
        plottables.clear();
        collection.forEach(plottable -> {
            invalidateNames.add(plottable.getName());
            plottable.addListener(AbstractPlotFrame.this);
            plottables.put(plottable.getName(), plottable);
        });
        invalidateNames.forEach(name -> {
            updatePlotData(name);
            updatePlotConfig(name);
        });
        list.invalidate();
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
