package hep.dataforge.plots;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.io.envelopes.*;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Name;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.utils.ReferenceRegistry;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;
import static hep.dataforge.plots.Plot.Wrapper.PLOT_WRAPPER_TYPE;

/**
 * A group of plottables. It could store Plots as well as other plot groups.
 */
public class PlotGroup extends SimpleConfigurable implements Plottable, Provider {
    public static final String PLOT_TARGET = "plot";


    private final String name;
    private NodeDescriptor descriptor = new NodeDescriptor("group");

    private Map<String, Plottable> plots = new HashMap<>();
    private ReferenceRegistry<PlotStateListener> listeners = new ReferenceRegistry<>();

    private PlotStateListener listener = new PlotStateListener() {
        @Override
        public void notifyDataChanged(String name) {
            dataChanged(name);
        }

        @Override
        public void notifyConfigurationChanged(String name) {
            configChanged(name);
        }

        @Override
        public void notifyGroupChanged(String name) {
            groupChanged(name);
        }
    };

    public PlotGroup(String name) {
        this.name = name;
    }

    public PlotGroup(String name, NodeDescriptor descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return name;
    }

    private void dataChanged(String name) {
        if (getName().isEmpty()) {
            //for root group
            listeners.forEach(l -> l.notifyDataChanged(name));
        } else {
            listeners.forEach(l -> l.notifyDataChanged(Name.joinString(getName(), name)));
        }
    }

    private void groupChanged(String name) {
        if (getName().isEmpty()) {
            //for root group
            listeners.forEach(l -> l.notifyGroupChanged(name));
        } else {
            listeners.forEach(l -> l.notifyGroupChanged(Name.joinString(getName(), name)));
        }
    }

    private void configChanged(String name) {
        if (getName().isEmpty()) {
            //for root group
            listeners.forEach(l -> l.notifyConfigurationChanged(name));
        } else {
            listeners.forEach(l -> l.notifyConfigurationChanged(Name.joinString(getName(), name)));
        }
    }

    /**
     * Get layered plot meta for plot with given name.
     *
     * @param path
     * @return
     */
    public Optional<Laminate> getPlotMeta(Name path) {
        if (path.length() == 0) {
            return Optional.of(new Laminate(getConfig()));
        } else if (path.length() == 1) {
            return opt(path).map(plot -> new Laminate(plot.getConfig(), getConfig()));
        } else {
            return opt(path.getFirst())
                    .flatMap(group -> getPlotMeta(path.cutFirst()))
                    .map(laminate -> laminate.withFirstLayer(getConfig()));
        }
    }

    public synchronized PlotGroup add(Plottable plot) {
        this.plots.put(plot.getName(), plot);
        plot.addListener(listener);
        groupChanged(plot.getName());
        return this;
    }

    /**
     * Recursive remove a plot
     *
     * @param path
     * @return
     */
    public synchronized PlotGroup remove(String path) {
        Name name = Name.of(path);
        opt(name.cutLast()).ifPresent(group -> {
            Plottable removed = plots.remove(name.getLast().toString());
            if (removed != null) {
                removed.removeListener(listener);
                groupChanged(path);
            }
        });
        return this;
    }

    @ProvidesNames(PLOT_TARGET)
    public Stream<String> list() {
        return plots.values().stream().flatMap(pl -> {
            if (pl instanceof PlotGroup) {
                return ((PlotGroup) pl).list().map(it -> Name.joinString(pl.getName(), it));
            } else {
                return Stream.of(pl.getName());
            }
        });
    }

    /**
     * Stream of all plots excluding intermediate nodes
     * @return
     */
    public Stream<Pair<String, Plottable>> stream() {
        return plots.values().stream().flatMap(pl -> {
            if (pl instanceof PlotGroup) {
                return ((PlotGroup) pl).stream().map(pair -> new Pair<>(Name.joinString(pl.getName(), pair.getKey()), pair.getValue()));
            } else {
                return Stream.of(new Pair<>(pl.getName(), pl));
            }
        });
    }

    /**
     * Recursively apply action to all children
     *
     * @param action
     */
    public void forEach(Consumer<Plottable> action) {
        this.plots.values().stream()
                .flatMap(it -> Stream.concat(Stream.of(it), it.getChildren().values().stream()))
                .forEach(action);
    }

    @Provides(PLOT_TARGET)
    public Optional<Plottable> opt(String name) {
        return opt(Name.of(name));
    }

    public Optional<Plottable> opt(Name name) {
        if (name.length() == 0) {
            throw new RuntimeException("Zero length names are not allowed");
        } else if (name.length() == 1) {
            return Optional.ofNullable(plots.get(name.toString()));
        } else {
            return opt(name.cutLast()).flatMap(plot -> {
                if (plot instanceof PlotGroup) {
                    return ((PlotGroup) plot).opt(name);
                } else {
                    return Optional.empty();
                }
            });
        }
    }

    /**
     * Add plottable state listener
     *
     * @param listener
     */
    public void addListener(PlotStateListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove plottable state listener
     *
     * @param listener
     */
    public void removeListener(PlotStateListener listener) {
        listeners.remove(listener);
    }

    @Override
    protected void applyConfig(Meta config) {
        super.applyConfig(config);
        listeners.forEach(l -> l.notifyConfigurationChanged(getName()));
    }

    @Override
    public Map<String, Plottable> getChildren() {
        return Collections.unmodifiableMap(plots);
    }

    @Override
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
    }


    public static class Wrapper implements hep.dataforge.io.envelopes.Wrapper<PlotGroup> {
        public static final String PLOT_GROUP_WRAPPER_TYPE = "df.plots.group";
        private static Plot.Wrapper plotWrapper = new Plot.Wrapper();

        @Override
        public String getName() {
            return PLOT_GROUP_WRAPPER_TYPE;
        }

        @Override
        public Class<PlotGroup> getType() {
            return PlotGroup.class;
        }

        @Override
        public Envelope wrap(PlotGroup group) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            EnvelopeWriter writer = DefaultEnvelopeType.instance.getWriter();

            for (Plottable plot : group.plots.values()) {
                try {
                    Envelope env;
                    if (plot instanceof PlotGroup) {
                        env = wrap((PlotGroup) plot);
                    } else if (plot instanceof Plot) {
                        env = plotWrapper.wrap((Plot) plot);
                    } else {
                        throw new RuntimeException("Unknown plottable type");
                    }
                    writer.write(baos, env);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write plot group to envelope", ex);
                }
            }

            EnvelopeBuilder builder = new EnvelopeBuilder()
                    .putMetaValue(WRAPPER_KEY, PLOT_GROUP_WRAPPER_TYPE)
                    .putMetaValue("name", group.getName())
                    .putMetaNode(DEFAULT_META_NAME, group.getConfig())
                    .setContentType("wrapper")
                    .setData(baos.toByteArray());

            if (group.getDescriptor() != null) {
                builder.putMetaNode("descriptor", group.getDescriptor().toMeta());
            }
            return builder.build();
        }

        @Override
        public PlotGroup unWrap(Envelope envelope) {
            checkValidEnvelope(envelope);
            String groupName = envelope.meta().getString("name");
            Meta groupMeta = envelope.meta().getMeta(DEFAULT_META_NAME);
            PlotGroup group = new PlotGroup(groupName);
            group.configure(groupMeta);

            EnvelopeType internalEnvelopeType = EnvelopeType.resolve(envelope.meta().getString("envelopeType", "default"));

            try {
                //Buffering stream to avoid rebufferization
                BufferedInputStream dataStream = new BufferedInputStream(envelope.getData().getStream());

                while (dataStream.available() > 0) {
                    Envelope item = internalEnvelopeType.getReader().read(dataStream);

                    if (item.meta().getString(WRAPPER_KEY).equals(PLOT_GROUP_WRAPPER_TYPE)) {
                        group.add(unWrap(item));
                    } else if (item.meta().getString(WRAPPER_KEY).equals(PLOT_WRAPPER_TYPE)) {
                        try {
                            group.add(plotWrapper.unWrap(item));
                        } catch (Exception ex) {
                            LoggerFactory.getLogger(getClass()).error("Failed to unwrap plottable");
                        }
                    }
                }

                return group;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
