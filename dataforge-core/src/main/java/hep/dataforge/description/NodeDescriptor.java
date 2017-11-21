/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.utils.SimpleMetaMorph;

import java.util.*;

/**
 * Descriptor for meta node. Could contain additional information for viewing
 * and editing.
 *
 * @author Alexander Nozik
 */
public class NodeDescriptor extends SimpleMetaMorph implements Named {

    public NodeDescriptor(String name) {
        super(Meta.buildEmpty(name));
    }

    public NodeDescriptor(Meta meta) {
        super(meta);
    }

    /**
     * True if multiple children with this nodes name are allowed. Anonimous
     * nodes are always single
     *
     * @return
     */
    public boolean isMultiple() {
        return getMeta().getBoolean("multiple", true) || isAnonimous();
    }

    /**
     * True if the node is required
     *
     * @return
     */
    public boolean isRequired() {
        return getMeta().getBoolean("required", false);
    }

    /**
     * The node description
     *
     * @return
     */
    public String info() {
        return getMeta().getString("info", "");
    }

    /**
     * The list of value descriptors
     *
     * @return
     */
    public Map<String, ValueDescriptor> valueDescriptors() {
        Map<String, ValueDescriptor> map = new HashMap<>();
        if (getMeta().hasMeta("value")) {
            for (Meta valueNode : getMeta().getMetaList("value")) {
                ValueDescriptor vd = new ValueDescriptor(valueNode);
                map.put(vd.getName(), vd);
            }
        }
        return map;
    }

    /**
     * The value descriptor for given value name. Null if there is no such value
     * descriptor
     *
     * @param name
     * @return
     */
    public ValueDescriptor valueDescriptor(String name) {
        return valueDescriptors().get(name);
    }

    /**
     * The map of children node descriptors
     *
     * @return
     */
    public Map<String, NodeDescriptor> childrenDescriptors() {
        Map<String, NodeDescriptor> map = new HashMap<>();
        if (getMeta().hasMeta("node")) {
            for (Meta node : getMeta().getMetaList("node")) {
                NodeDescriptor nd = new NodeDescriptor(node);
                map.put(nd.getName(), nd);
            }
        }
        return map;
    }

    /**
     * Check if this node has default
     *
     * @return
     */
    public boolean hasDefault() {
        return getMeta().hasMeta("default");
    }

    /**
     * A list of tags for this node. Tags used to customize node usage
     *
     * @return
     */
    public List<String> tags() {
        if (getMeta().hasValue("tags")) {
            return Arrays.asList(getMeta().getStringArray("tags"));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * The default meta for this node (could be multiple). Null if not defined
     *
     * @return
     */
    public List<? extends Meta> defaultNode() {
        if (getMeta().hasMeta("default")) {
            return getMeta().getMetaList("default");
        } else {
            return null;
        }
    }

    /**
     * The child node descriptor for given name
     *
     * @param name
     * @return
     */
    public NodeDescriptor childDescriptor(String name) {
        return childrenDescriptors().get(name);
    }

    /**
     * The name of this node
     *
     * @return
     */
    @Override
    public String getName() {
        return getMeta().getString("name", getMeta().getName());
    }

    /**
     * Identify if this descriptor has child value descriptor with default
     *
     * @param name
     * @return
     */
    public boolean hasDefaultForValue(String name) {
        ValueDescriptor desc = valueDescriptor(name);
        return desc != null && desc.hasDefault();
    }

    /**
     * The key of the value which is used to display this node in case it is
     * multiple. By default, the key is empty which means that node index is
     * used.
     *
     * @return
     */
    public String titleKey() {
        return getMeta().getString("titleKey", "");
    }

}
