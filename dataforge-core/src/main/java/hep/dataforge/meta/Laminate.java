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
package hep.dataforge.meta;

import hep.dataforge.description.Described;
import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Named;
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A chain of immutable meta. The value is taken from the first meta in list
 * that contains it. The list itself is immutable.
 *
 * @author darksnake
 */
public final class Laminate extends Meta implements Described {

    private final List<Meta> layers = new ArrayList<>();
    private NodeDescriptor descriptor;
    private Meta descriptorLayer;

    public Laminate(List<Meta> layers) {
        layers.forEach(this::addLayer);
    }

    /**
     * Create laminate from layers. Deepest first.
     *
     * @param layers
     */
    public Laminate(Meta... layers) {
        this(Arrays.asList(layers));
    }

    /**
     * Copy constructor
     *
     * @param laminate
     */
    public Laminate(Laminate laminate) {
        this.layers.addAll(laminate.layers);
        this.descriptor = laminate.descriptor;
        this.descriptorLayer = laminate.descriptorLayer;
    }

    private void addLayer(Meta layer) {
        if (layer != null && !layer.isEmpty()) {
            if (layer instanceof MutableMetaNode) {
                LoggerFactory.getLogger(getClass()).trace("Using mutable meta in the laminate");
            }
            this.layers.add(layer);
        }
    }

    private void addFirstLayer(Meta layer) {
        if (layer != null && !layer.isEmpty()) {
            if (layer instanceof MutableMetaNode) {
                LoggerFactory.getLogger(getClass()).trace("Using mutable meta in the laminate");
            }
            this.layers.add(0, layer);
        }
    }

    private void setDescriptor(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
        /*
         * Add node descriptor as a separate laminate layer to avoid including it in
         * hasMeta and hasValue
         */
        descriptorLayer = DescriptorUtils.buildDefaultNode(descriptor);
    }

    @Contract(pure = true)
    public boolean hasDescriptor() {
        return this.descriptor != null;
    }

    /**
     * Attach descriptor to this laminate to use for default values and aliases
     * (ALIASES NOT IMPLEMENTED YET!).
     */
    public Laminate withDescriptor(NodeDescriptor descriptor) {
        Laminate res = new Laminate(this);
        res.setDescriptor(descriptor);
        return res;
    }

    /**
     * Add primary (first layer)
     *
     * @param layer
     * @return
     */
    public Laminate withFirstLayer(Meta layer) {
        if (layer.isEmpty()) {
            return this;
        } else {
            Laminate res = new Laminate(this);
            res.addFirstLayer(layer);
            return res;
        }
    }

    /**
     * Add layer to stack
     *
     * @param layers
     * @return
     */
    public Laminate withLayer(Meta... layers) {
        Laminate res = new Laminate(this);
        for (Meta layer : layers) {
            res.addLayer(layer);
        }
        return res;
    }

    public List<Meta> layers() {
        return Collections.unmodifiableList(layers);
    }

    /**
     * Get laminate layers in inverse order
     *
     * @return
     */
    public List<Meta> layersInverse() {
        List<Meta> layersInverse = new ArrayList<>(this.layers);
        Collections.reverse(layersInverse);
        return layersInverse;
    }

    @Override
    public Optional<Meta> optMeta(String path) {
        List<Meta> childLayers = new ArrayList<>();
        layers.stream().filter(layer -> layer.hasMeta(path)).forEach((m) -> {
            //FIXME child elements are not chained!
            childLayers.add(m.getMeta(path));
        });
        if (!childLayers.isEmpty()) {
            Laminate laminate = new Laminate(childLayers);
            //adding child node descriptor to the child laminate
            if (descriptor != null && descriptor.childrenDescriptors().containsKey(path)) {
                laminate.setDescriptor(descriptor.childDescriptor(path));
            }
            return Optional.of(laminate);
        } else //if node not found, using descriptor layer if it is defined
            if (descriptorLayer != null) {
                return descriptorLayer.optMeta(path);
            } else {
                return Optional.empty();
            }
    }


    /**
     * Get the first occurrence of meta node with the given name without merging. If not found, uses description.
     *
     * @param path
     * @return
     */
    @Override
    public List<? extends Meta> getMetaList(String path) {
        return Stream.concat(layers.stream(), Stream.of(descriptorLayer))
                .filter((m) -> (m.hasMeta(path)))
                .map(m -> m.getMetaList(path)).findFirst()
                .orElseThrow(() -> new NameNotFoundException(path));
    }

    @Override
    public String getName() {
        return layers().stream().map(Named::getName).findFirst().orElse(MetaNode.DEFAULT_META_NAME);
    }

    /**
     * Node names includes descriptor nodes
     *
     * @return
     */
    @Override
    public Stream<String> getNodeNames(boolean includeHidden) {
        return getNodeNames(includeHidden, true);
    }


    public Stream<String> getNodeNames(boolean includeHidden, boolean includeDefaults) {
        Stream<String> names = layers.stream().flatMap(layer -> layer.getNodeNames(includeHidden)).distinct();
        if (includeDefaults && descriptorLayer != null) {
            return Stream.concat(names, descriptorLayer.getNodeNames(includeHidden));
        } else {
            return names;
        }
    }

    /**
     * Value names includes descriptor values,
     *
     * @return
     */
    @Override
    public Stream<String> getValueNames(boolean includeHidden) {
        return getValueNames(includeHidden, true);
    }

    public Stream<String> getValueNames(boolean includeHidden, boolean includeDefaults) {
        Stream<String> names = layers.stream().flatMap(layer -> layer.getValueNames(includeHidden)).distinct();
        if (includeDefaults && descriptorLayer != null) {
            return Stream.concat(names, descriptorLayer.getValueNames(includeHidden));
        } else {
            return names;
        }
    }

    @Override
    public Optional<Value> optValue(String path) {
        Optionals<Value> opts = Optionals.either();

        //searching layers for value
        for (Meta m : layers) {
            opts = opts.or(() -> m.optValue(path));
        }

        // if descriptor layer is definded, searching it for value
        if (descriptorLayer != null) {
            opts = opts.or(() -> descriptorLayer.optValue(path));
        }

        return opts.opt().map(it -> MetaUtils.transformValue(it));
    }

    @Override
    public NodeDescriptor getDescriptor() {
        if (descriptor != null) {
            return descriptor;
        } else {
            //TODO consider descriptor merging
            return layers.stream()
                    .filter(it -> it instanceof Laminate).map(Laminate.class::cast)
                    .map(Laminate::getDescriptor)
                    .findFirst().orElse(new NodeDescriptor(Meta.empty()));

        }
    }

    @Override
    public boolean isEmpty() {
        return this.layers.isEmpty() && (this.descriptorLayer == null || this.descriptorLayer.isEmpty());
    }

    /**
     * Combine values in layers using provided collector. Default values from provider and description are ignored
     *
     * @param valueName
     * @param collector
     * @return
     */
    public Value collectValue(String valueName, Collector<Value, ?, Value> collector) {
        return layers.stream()
                .filter(layer -> layer.hasValue(valueName))
                .map(layer -> layer.getValue(valueName))
                .collect(collector);
    }

    /**
     * Merge nodes using provided collector (good idea to use {@link MergeRule}).
     *
     * @param nodeName
     * @param collector
     * @param <A>
     * @return
     */
    public <A> Meta collectNode(String nodeName, Collector<Meta, A, Meta> collector) {
        return layers.stream()
                .filter(layer -> layer.hasMeta(nodeName))
                .map(layer -> layer.getMeta(nodeName))
                .collect(collector);
    }

    /**
     * Merge node lists grouping nodes by provided classifier and then merging each group independently
     *
     * @param nodeName   the name of node
     * @param classifier grouping function
     * @param collector  used to each group
     * @param <A>        intermediate collector accumulator type
     * @param <K>        classifier key type
     * @return
     */
    public <A, K> Collection<Meta> collectNodes(String nodeName, Function<? super Meta, ? extends K> classifier, Collector<Meta, A, Meta> collector) {
        return layers().stream()
                .filter(layer -> layer.hasMeta(nodeName))
                .flatMap(layer -> layer.getMetaList(nodeName).stream())
                .collect(Collectors.groupingBy(classifier, LinkedHashMap::new, collector)).values();
        //linkedhashmap ensures ordering
    }

    /**
     * Same as above, but uses fixed replace rule to merge meta
     *
     * @param nodeName
     * @param classifier
     * @param <K>
     * @return
     */
    public <K> Collection<Meta> collectNodes(String nodeName, Function<? super Meta, ? extends K> classifier) {
        return collectNodes(nodeName, classifier, MergeRule.replace());
    }

    /**
     * Same as above but uses fixed meta value with given key as identity
     *
     * @param nodeName
     * @param key
     * @return
     */
    public Collection<Meta> collectNodes(String nodeName, String key) {
        return collectNodes(nodeName, meta -> getValue(key, Value.NULL));
    }

    /**
     * Calculate sum of numeric values with given name. Values in all layers must be numeric.
     *
     * @param valueName
     * @return
     */
    public double sumValue(String valueName) {
        return layers.stream().mapToDouble(layer -> layer.getDouble(valueName, 0)).sum();
    }

    /**
     * Press all of the Laminate layers together creating single immutable meta
     *
     * @return
     */
    @NotNull
    public Meta merge() {
        return new SealedNode(this);
    }

    /**
     * deep flat map of laminate layers
     *
     * @return
     */
    private Stream<Meta> deepLayerStream() {
        return this.layers.stream().flatMap(layer -> {
            if (layer instanceof Laminate) {
                return ((Laminate) layer).deepLayerStream();
            } else {
                return Stream.of(layer);
            }
        });
    }

    /**
     * Return cleaned up laminate eliminating needless substructures
     *
     * @return
     */
    public Laminate cleanup() {
        List<Meta> resLayers = deepLayerStream().collect(Collectors.toList());

        Laminate res = new Laminate(resLayers);

        res.setDescriptor(getDescriptor());

        return res;
    }
}
