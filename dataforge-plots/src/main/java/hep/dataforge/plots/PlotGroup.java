package hep.dataforge.plots;

import hep.dataforge.description.DescriptorUtils;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;

/**
 * A group of plottables. It could store Plots as well as other plot groups.
 */
public class PlotGroup extends SimpleConfigurable implements Plottable, Provider, PlotStateListener, Iterable<Plottable> {
    public static final String PLOT_TARGET = "plot";

    public static final Wrapper WRAPPER = new Wrapper();

    private final String name;
    private NodeDescriptor descriptor = new NodeDescriptor("group");

    private Map<String, Plottable> plots = new HashMap<>();
    private ReferenceRegistry<PlotStateListener> listeners = new ReferenceRegistry<>();


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

    @Override
    public void notifyDataChanged(String name) {
        if (getName().isEmpty()) {
            //for root group
            listeners.forEach(l -> l.notifyDataChanged(name));
        } else {
            listeners.forEach(l -> l.notifyDataChanged(Name.joinString(getName(), name)));
        }
    }

    @Override
    public void notifyConfigurationChanged(String name) {
        if (getName().isEmpty()) {
            //for root group
            listeners.forEach(l -> l.notifyConfigurationChanged(name));
        } else {
            listeners.forEach(l -> l.notifyConfigurationChanged(Name.joinString(getName(), name)));
        }
    }

    @Override
    public void notifyGroupChanged(String name) {
        if (getName().isEmpty()) {
            //for root group
            listeners.forEach(l -> l.notifyGroupChanged(name));
        } else {
            listeners.forEach(l -> l.notifyGroupChanged(Name.joinString(getName(), name)));
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
            return opt(path).map(plot ->
                    new Laminate(plot.getConfig(), getConfig())
                            .withDescriptor(DescriptorUtils.buildDescriptor(plot.getClass()))
            );
        } else {
            return opt(path.getFirst())
                    .flatMap(group -> getPlotMeta(path.cutFirst()))
                    .map(laminate -> laminate.withFirstLayer(getConfig()));
        }
    }

    public synchronized PlotGroup add(Plottable plot) {
        this.plots.put(plot.getName(), plot);
        plot.addListener(this);
        notifyGroupChanged(plot.getName());
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
        if (name.length() == 1) {
            Plottable removed = plots.remove(name.getLast().toString());
            if (removed != null) {
                removed.removeListener(this);
                notifyGroupChanged(path);
            }
        } else {
            opt(name.cutLast()).ifPresent(group -> {
                if (group instanceof PlotGroup) {
                    ((PlotGroup) group).remove(name.getLast().toString());
                }
            });
        }
        return this;
    }

    public void clear() {
        new HashSet<>(this.plots.keySet()).forEach(this::remove);
    }

    @ProvidesNames(PLOT_TARGET)
    public Stream<String> list() {
        return stream().map(Pair::getKey);
    }

    /**
     * Stream of all plots excluding intermediate nodes
     *
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

    @Provides(PLOT_TARGET)
    public Optional<Plottable> opt(String name) {
        return opt(Name.of(name));
    }

    public boolean has(String name) {
        return opt(name).isPresent();
    }

    public Optional<Plottable> opt(Name name) {
        if (name.length() == 0) {
            throw new RuntimeException("Zero length names are not allowed");
        } else if (name.length() == 1) {
            return Optional.ofNullable(plots.get(name.toString()));
        } else {
            return opt(name.cutLast()).flatMap(plot -> {
                if (plot instanceof PlotGroup) {
                    return ((PlotGroup) plot).opt(name.getLast());
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
    public Collection<Plottable> getChildren() {
        return plots.values();
    }

    @Override
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Iterate over direct descendants
     *
     * @return
     */
    @NotNull
    @Override
    public Iterator<Plottable> iterator() {
        return this.plots.values().iterator();
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
                    .putMetaValue(WRAPPER_TYPE_KEY, PLOT_GROUP_WRAPPER_TYPE)
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
            //checkValidEnvelope(envelope);
            String groupName = envelope.meta().getString("name");
            Meta groupMeta = envelope.meta().getMetaOrEmpty(DEFAULT_META_NAME);
            PlotGroup group = new PlotGroup(groupName);
            group.configure(groupMeta);

            EnvelopeType internalEnvelopeType = EnvelopeType.resolve(envelope.meta().getString("@envelope.internalType", "default"));

            try {
                InputStream dataStream = envelope.getData().getStream();

                while (dataStream.available() > 0) {
                    Envelope item = internalEnvelopeType.getReader().read(dataStream);
                    try {
                        Plottable pl = Plottable.class.cast(hep.dataforge.io.envelopes.Wrapper.unwrap(item));
                        group.add(pl);
                    } catch (Exception ex) {
                        LoggerFactory.getLogger(getClass()).error("Failed to unwrap plottable", ex);
                    }
                }

                return group;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
