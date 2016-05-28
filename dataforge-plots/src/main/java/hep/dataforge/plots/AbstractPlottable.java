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
import static hep.dataforge.io.envelopes.Wrappable.DEFAULT_WRAPPER_ENVELOPE_CODE;
import static hep.dataforge.io.envelopes.Wrappable.WRAPPED_TYPE_KEY;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.tables.PointAdapter;
import hep.dataforge.utils.NonNull;
import hep.dataforge.utils.ReferenceRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 *
 * @author darksnake
 */
public abstract class AbstractPlottable<T extends PointAdapter> extends SimpleConfigurable implements Plottable<T> {

    public static final String ADAPTER_KEY = "adapter";
    public static final String PLOTTABLE_WRAPPER_TYPE = "plottable";

    private final String name;
    private ReferenceRegistry<PlotStateListener> listeners = new ReferenceRegistry<>();
    private T adapter;

    public AbstractPlottable(String name, @NonNull T adapter) {
        this(name);
        setAdapter(adapter);
    }

    public AbstractPlottable(@NonNull String name) {
        this.name = name;
    }

    @Override
    public void addListener(@NonNull PlotStateListener listener) {
        getListeners().add(listener);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void removeListener(@NonNull PlotStateListener listener) {
        this.getListeners().remove(listener);
    }

    @Override
    public T adapter() {
        if (adapter == null) {
            return defaultAdapter();
        } else {
            return adapter;
        }
    }

    @Override
    public final void setAdapter(T adapter) {
        this.adapter = adapter;
        //Silently update meta to include adapter
        this.getConfig().putNode(ADAPTER_KEY, adapter.meta(), false);
    }

    @Override
    public final void setAdapter(Meta adapterMeta) {
        setAdapter(buildAdapter(adapterMeta));
    }

    protected abstract T buildAdapter(Meta adapterMeta);

    protected abstract T defaultAdapter();

    /**
     * Notify all listeners that configuration changed
     *
     * @param config
     */
    @Override
    protected void applyConfig(Meta config) {
        getListeners().forEach((l) -> l.notifyConfigurationChanged(getName()));
        //If adapter is not defined, creating new adapter.
        if (this.adapter == null && config.hasNode(ADAPTER_KEY)) {
            setAdapter(config.getNode(ADAPTER_KEY));
        }
    }

    /**
     * Notify all listeners that data changed
     */
    public void notifyDataChanged() {
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
     * @return 
     */
    protected EnvelopeBuilder wrapBuilder() {
        EnvelopeBuilder builder = new EnvelopeBuilder()
                .setDataType("df.plots.Plottable")
                .putMetaValue(WRAPPED_TYPE_KEY, PLOTTABLE_WRAPPER_TYPE)
                .putMetaValue("plottableClass", getClass().getName())
                .putMetaValue("name", getName())
                .putMetaNode("meta", meta())
                .setEnvelopeType(DEFAULT_WRAPPER_ENVELOPE_CODE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
            os.writeObject(this.data());

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

}
