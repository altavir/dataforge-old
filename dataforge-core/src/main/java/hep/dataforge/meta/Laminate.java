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

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 * A chain of immutable meta. The value is taken from the first meta in list
 * that contains it.
 * <p>
 * TODO make customizable merging procedures
 * </p>
 *
 * @author darksnake
 */
public class Laminate extends Meta {

    private String name;
    private List<Meta> layers;
    private NodeDescriptor descriptor;
    private Meta descriptorLayer;
    private ValueProvider valueContext;

    public Laminate(String name) {
        this.name = name;
        this.layers = new ArrayList<>();
    }

    public Laminate(String name, List<Meta> layers) {
        this.name = name;
        this.layers = new ArrayList(layers);
        this.layers.removeIf((meta) -> meta == null);
    }

    public Laminate(List<Meta> layers) {
        this(layers == null || layers.isEmpty() ? "" : layers.get(0).getName(), layers);
    }

    public Laminate(Meta... layers) {
        this(Arrays.asList(layers));
    }

    public Laminate(String name, Meta... layers) {
        this(name, Arrays.asList(layers));
    }

    public ValueProvider valueContext() {
        return valueContext;
    }

    /**
     * Attach descriptor to this laminate to use for default values and aliases
     * (ALIASES NOT IMPLEMENTED YET!).
     */
    public Laminate setDescriptor(NodeDescriptor descriptor) {
        //Storing descriptor to pass it to children 
        this.descriptor = descriptor;
        buildDescriptorLayer();
        return this;
    }

    /**
     * Add node descriptor as a separate laminate layer to avoid including it in
     * hasNode and hasValue
     */
    private void buildDescriptorLayer() {

        if (this.descriptor != null) {
            descriptorLayer = DescriptorUtils.buildDefaultNode(descriptor);
        }
    }

    public Laminate setValueContext(ValueProvider valueContext) {
        this.valueContext = valueContext;
        return this;
    }

    /**
     * Add primary (first layer)
     *
     * @param layer
     * @return
     */
    public Laminate addFirstLayer(Meta layer) {
        this.layers.add(0, layer);
        return this;
    }

    /**
     * Add layer to stack
     *
     * @param layer
     * @return
     */
    public Laminate addLayer(Meta layer) {
        this.layers.add(layer);
        return this;
    }

    public Laminate setLayers(Meta... layers) {
        this.layers.clear();
        this.layers.addAll(Arrays.asList(layers));
        this.layers.removeIf((meta) -> meta == null);
        return this;
    }

    public Laminate setLayers(Collection<Meta> layers) {
        this.layers.clear();
        this.layers.addAll(layers);
        this.layers.removeIf((meta) -> meta == null);
        return this;
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
    public Meta getNode(String path) {
        List<Meta> childLayers = new ArrayList<>();
        layers.stream().filter((m) -> (m.hasNode(path))).forEach((m) -> {
            //FIXME child elements are not chained!
            childLayers.add(m.getNode(path));
        });
        if (!childLayers.isEmpty()) {
            Laminate laminate = new Laminate(childLayers);
            //adding child node descriptor to the child laminate
            if (descriptor != null && descriptor.childrenDescriptors().containsKey(path)) {
                laminate.setDescriptor(descriptor.childDescriptor(path));
            }
            laminate.setValueContext(valueContext());
            return laminate;
        } else //if node not found, using descriptor layer if it is defined
         if (descriptorLayer != null) {
                return descriptorLayer.getNode(path);
            } else {
                throw new NameNotFoundException(path);
            }
    }

    @Override
    public List<? extends Meta> getNodes(String path) {
        List<List<? extends Meta>> childLayers = new ArrayList<>();
        layers.stream().filter((m) -> (m.hasNode(path))).forEach((m) -> {
            childLayers.add(m.getNodes(path));
        });
        if (!childLayers.isEmpty()) {
            if (childLayers.size() > 1) {
                LoggerFactory.getLogger(getClass())
                        .warn("Using laminate to merge lists of nodes. Node list merging is not currently supported");
            }
            return childLayers.get(0);
        } else //if node not found, using descriptor layer if it is defined
         if (descriptorLayer != null) {
                return descriptorLayer.getNodes(path);
            } else {
                throw new NameNotFoundException(path);
            }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Node names includes descriptor nodes
     *
     * @return
     */
    @Override
    public Collection<String> getNodeNames() {
        return getNodeNames(true);
    }
    
    
    public Collection<String> getNodeNames(boolean includeDefaults) {
        Set<String> names = new HashSet<>();
        if (layers != null) {
            layers.stream().forEach((m) -> {
                names.addAll(m.getNodeNames());
            });
        }

        if (includeDefaults && descriptorLayer != null) {
            names.addAll(descriptorLayer.getNodeNames());
        }

        return names;
    }

    /**
     * Value names includes descriptor values,
     *
     * @return
     */
    @Override
    public Collection<String> getValueNames() {
        return getValueNames(true);
    }

    public Collection<String> getValueNames(boolean includeDefaults) {
        Set<String> names = new HashSet<>();
        layers.stream().forEach((m) -> {
            names.addAll(m.getValueNames());
        });

        if (includeDefaults && descriptorLayer != null) {
            names.addAll(descriptorLayer.getValueNames());
        }

        return names;
    }

    @Override
    public Value getValue(String path) {
        //searching layers for value
        for (Meta m : layers) {
            if (m.hasValue(path)) {
                return MetaUtils.transformValue(m.getValue(path), valueContext());
            }
        }

        // if descriptor layer is definded, serching it for value
        if (descriptorLayer != null && descriptorLayer.hasValue(path)) {
            return MetaUtils.transformValue(descriptorLayer.getValue(path), valueContext());
        }

        throw new NameNotFoundException(path);
    }

}
