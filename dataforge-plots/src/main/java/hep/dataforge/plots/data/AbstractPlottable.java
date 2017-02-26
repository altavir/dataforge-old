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
package hep.dataforge.plots.data;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.plots.PlotStateListener;
import hep.dataforge.plots.Plottable;
import hep.dataforge.tables.PointAdapter;
import hep.dataforge.utils.ReferenceRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author darksnake
 */
public abstract class AbstractPlottable<T extends PointAdapter> extends SimpleConfigurable implements Plottable {

    public static final String ADAPTER_KEY = "adapter";
    public static final String PLOTTABLE_WRAPPER_TYPE = "plottable";

    private final String name;
    private ReferenceRegistry<PlotStateListener> listeners = new ReferenceRegistry<>();
    private T adapter;

//    public AbstractPlottable(String name, @NotNull T adapter) {
//        this(name);
//        setAdapter(adapter);
//    }

    public AbstractPlottable(@NotNull String name) {
        this.name = name;
    }

    @Override
    public void addListener(@NotNull PlotStateListener listener) {
        getListeners().add(listener);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void removeListener(@NotNull PlotStateListener listener) {
        this.getListeners().remove(listener);
    }

    @Override
    public T getAdapter() {
        //If adapter is not defined, creating new adapter.
        if (adapter == null) {
            adapter = buildAdapter(meta().getMeta(ADAPTER_KEY, Meta.empty()));
        }
        return adapter;
    }

    protected abstract T buildAdapter(Meta adapterMeta);

    /**
     * Notify all listeners that configuration changed
     *
     * @param config
     */
    @Override
    protected synchronized void applyConfig(Meta config) {
        getListeners().forEach((l) -> l.notifyConfigurationChanged(getName()));

        //invalidate adapter
        if (config.hasMeta(ADAPTER_KEY)) {
            adapter = null;
        }
    }

    /**
     * Notify all listeners that data changed
     */
    public synchronized void notifyDataChanged() {
        getListeners().forEach((l) -> l.notifyDataChanged(getName()));
    }

    public String getTitle() {
        return meta().getString("title", getName());
    }

    @Override
    public Envelope wrap() {
        return wrapBuilder().build();
    }

    /**
     * Protected method to customize wrap
     *
     * @return
     */
    protected EnvelopeBuilder wrapBuilder() {
        EnvelopeBuilder builder = new EnvelopeBuilder()
                .setDataType("df.plots.Plottable")
                .putMetaValue(WRAPPED_TYPE_KEY, PLOTTABLE_WRAPPER_TYPE)
                .putMetaValue("plottableClass", getClass().getName())
                .putMetaValue("name", getName())
                .putMetaNode("meta", new Laminate(meta()).setDescriptor(DescriptorUtils.buildDescriptor(this)))
                .setEnvelopeType(WRAPPER_ENVELOPE_CODE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
            os.writeObject(getData());

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        builder.setData(baos.toByteArray());
        return builder;
    }

    /**
     * @return the listeners
     */
    private ReferenceRegistry<PlotStateListener> getListeners() {
        return listeners;
    }

    public void setAdapter(T adapter) {
        this.configureNode(ADAPTER_KEY, adapter.meta());
    }

}
