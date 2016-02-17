/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.meta.Configuration;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;

public class MetaTreeBranch extends MetaTree {

    MetaTreeBranch parent;
    Configuration node;
    NodeDescriptor descriptor;

    private final ObservableBooleanValue isDefaultValue = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return (node == null || node.isEmpty()) && descriptor != null;
        }
    };

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
        if (node != null) {
            return node.getName() + getIndex();
        } else if (descriptor != null) {
            return descriptor.getName();
        } else {
            return "";
        }
    }

    private String getIndex() {
        if (node != null && getParent() != null) {
            List<Configuration> list = getParent().getNode().getNodes(node.getName());
            if(list.size()>1){
                return String.format(" [%d]", list.indexOf(node));
            } else {
                return "";//single item node
            }
        } else {
            return "";//no node
        }
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
    public ObservableBooleanValue isDefault() {
        return isDefaultValue;
    }

    /**
     * Build new node from default if it was not existing before
     */
    void buildNode() {
        if (node == null && parent != null) {
            //building node
            this.node = new Configuration(getName());
            parent.getNode().attachNode(node);
        }
    }

    public MetaTreeBranch getParent() {
        return parent;
    }

    public Configuration getNode() {
        if (node == null) {
            buildNode();
        }
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
