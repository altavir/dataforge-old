/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.meta.Configuration;
import hep.dataforge.values.Value;

public class MetaTreeBranch implements MetaTree {

    MetaTreeBranch parent;
    Configuration node;
    NodeDescriptor descriptor;

    public MetaTreeBranch(MetaTreeBranch parent, Configuration node, NodeDescriptor descriptor) {
        this.parent = parent;
        this.node = node;
        this.descriptor = descriptor;
    }

    public void setDescriptor(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        if(node != null){
            return node.getName();
        } else if(descriptor!= null){
            return descriptor.getName();
        } else {
            return "";
        }
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public void setValue(Value value) {

    }

    @Override
    public String getDescription() {
        return descriptor == null ? null : descriptor.info();
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isDefault() {
        return (node == null || node.isEmpty()) && descriptor != null;
    }

    /**
     * Build new node from default if it was not existing before
     */
    void buildNode() {
        if (isDefault()) {
            //building node if empty
            parent.buildNode();
            //creating new configuration node
            this.node = new Configuration(getName(), parent.node);
            //adding this node to parent
            parent.node.putNode(node);
        }
    }

    public MetaTreeBranch getParent() {
        return parent;
    }

    public Configuration getNode() {
        return node;
    }

    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean hasDescriptor() {
        return descriptor != null;
    }
}
