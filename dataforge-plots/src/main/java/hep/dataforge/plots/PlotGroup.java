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
public class PlotGroup extends SimpleConfigurable implements Plottable, Provider, PlotListener, Iterable<Plottable> {
    public static final String PLOT_TARGET = "plot";

    public static final Wrapper WRAPPER = new Wrapper();

    private final Name name;
    private NodeDescriptor descriptor = NodeDescriptor.empty("group");

    private Map<Name, Plottable> plots = new HashMap<>();
    private ReferenceRegistry<PlotListener> listeners = new ReferenceRegistry<>();


    public PlotGroup(String name) {
        this.name = Name.ofSingle(name);
    }

    public PlotGroup(String name, NodeDescriptor descriptor) {
        this.name = Name.ofSingle(name);
        this.descriptor = descriptor;
    }

//    public PlotGroup(String name, NodeDescriptor descriptor) {
//        this.name = name;
//        this.descriptor = descriptor;
//    }

    @Override
    public Name getName() {
        return name;
    }

    private Name getNameForListener(Name arg) {
        return Name.join(name, arg);
    }

    @Override
    public void dataChanged(Name name, Plot plot) {
        listeners.forEach(l -> l.dataChanged(getNameForListener(name), plot));
    }

    @Override
    public void metaChanged(Name name, Plottable plottable, Laminate laminate) {
        listeners.forEach(l -> l.metaChanged(getNameForListener(name), plottable, laminate.withLayer(getConfig()).cleanup()));
    }

    @Override
    public void plotAdded(Name name, Plottable plottable) {
        listeners.forEach(l -> l.plotAdded(getNameForListener(name), plottable));
    }

    @Override
    public void plotRemoved(Name name) {
        listeners.forEach(l -> l.plotRemoved(getNameForListener(name)));
    }

    /**
     * Recursively notify listeners about all added plots
     *
     * @param plot
     */
    private void notifyPlotAdded(Plottable plot) {
        plotAdded(plot.getName(), plot);
        metaChanged(plot.getName(), plot, new Laminate(plot.getConfig()));
        if (plot instanceof PlotGroup) {
            ((PlotGroup) plot).getChildren().forEach(((PlotGroup) plot)::notifyPlotAdded);
        }
    }

    public synchronized PlotGroup add(Plottable plot) {
        this.plots.put(plot.getName(), plot);
        plot.addListener(this);

        notifyPlotAdded(plot);
        return this;
    }

    private void notifyPlotRemoved(Plottable plot) {
        if (plot instanceof PlotGroup) {
            ((PlotGroup) plot).getChildren().forEach(((PlotGroup) plot)::notifyPlotRemoved);
        }
        //remove children first
        plotRemoved(plot.getName());
    }

    /**
     * Recursive remove a plot
     *
     * @param name
     * @return
     */
    public synchronized PlotGroup remove(String name) {
        return remove(Name.ofSingle(name));
    }

    public synchronized PlotGroup remove(Name name) {
        if (name.getLength() == 1) {
            Plottable removed = plots.remove(name);
            if (removed != null) {
                notifyPlotRemoved(removed);
                removed.removeListener(this);
            }
        } else {
            opt(name.cutLast()).ifPresent(group -> {
                if (group instanceof PlotGroup) {
                    ((PlotGroup) group).remove(name.getLast());
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
        return stream().map(Pair::getKey).map(Name::toString);
    }

    /**
     * Stream of all plots excluding intermediate nodes
     *
     * @return
     */
    public Stream<Pair<Name, Plottable>> stream() {
        return plots.values().stream().flatMap(pl -> {
            if (pl instanceof PlotGroup) {
                return ((PlotGroup) pl).stream().map(pair -> new Pair<>(Name.join(pl.getName(), pair.getKey()), pair.getValue()));
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
        if (name.getLength() == 0) {
            throw new RuntimeException("Zero length names are not allowed");
        } else if (name.getLength() == 1) {
            return Optional.ofNullable(plots.get(name));
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
    public void addListener(PlotListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove plottable state listener
     *
     * @param listener
     */
    public void removeListener(PlotListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify that config for this element and children is changed
     */
    private void notifyConfigChanged() {
        metaChanged(Name.EMPTY, this, new Laminate(getConfig()).withDescriptor(descriptor));
        getChildren().forEach(pl -> {
            if (pl instanceof PlotGroup) {
                ((PlotGroup) pl).notifyConfigChanged();
            } else {
                metaChanged(pl.getName(), pl, new Laminate(pl.getConfig()).withDescriptor(pl.getDescriptor()));
            }
        });
    }

    @Override
    protected void applyConfig(Meta config) {
        super.applyConfig(config);
        notifyConfigChanged();
    }

    public Collection<Plottable> getChildren() {
        return plots.values();
    }

    @Override
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
        notifyConfigChanged();
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
            EnvelopeWriter writer = DefaultEnvelopeType.Companion.getINSTANCE().getWriter();

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
                    .setMetaValue(WRAPPER_TYPE_KEY, PLOT_GROUP_WRAPPER_TYPE)
                    .setMetaValue("name", group.getName())
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
            String groupName = envelope.getMeta().getString("name");
            Meta groupMeta = envelope.getMeta().getMetaOrEmpty(DEFAULT_META_NAME);
            PlotGroup group = new PlotGroup(groupName);
            group.configure(groupMeta);

            EnvelopeType internalEnvelopeType = EnvelopeType.Companion.resolve(envelope.getMeta().getString("@envelope.internalType", "default"));

            try {
                InputStream dataStream = envelope.getData().getStream();

                while (dataStream.available() > 0) {
                    Envelope item = internalEnvelopeType.getReader().read(dataStream);
                    try {
                        Plottable pl = Plottable.class.cast(hep.dataforge.io.envelopes.Wrapper.Companion.unwrap(item));
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
