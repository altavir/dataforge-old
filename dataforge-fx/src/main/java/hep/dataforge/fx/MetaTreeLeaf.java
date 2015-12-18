/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.description.ValueDescriptor;
import hep.dataforge.values.Value;

public class MetaTreeLeaf implements MetaTree {

    MetaTreeBranch parent;
    String name;
    boolean frozen = false;

    public MetaTreeLeaf(MetaTreeBranch parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Value getValue() {
        if (parent.node!=null && parent.node.hasValue(name)) {
            return parent.node.getValue(name);
        } else if(parent.getDescriptor() != null) {
            ValueDescriptor vd = parent.descriptor.valueDescriptor(name);
            if (vd == null) {
                return null;
            } else {
                return vd.defaultValue();
            }
        } else {
            return Value.getNull();
        }
    }

    @Override
    public void setValue(Value value) {
        if (parent.isDefault()) {
            parent.buildNode();
        }
        parent.node.setValue(name, value);
    }

    @Override
    public String getDescription() {
        if (parent.getDescriptor() == null) {
            return null;
        } else {
            ValueDescriptor vd = parent.getDescriptor().valueDescriptor(name);
            if (vd != null) {
                return vd.info();
            } else {
                return null;
            }
        }
    }

    public ValueDescriptor getDescriptor() {
        if (parent.getDescriptor() == null) {
            return null;
        } else {
            return parent.getDescriptor().valueDescriptor(name);
        }
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isDefault() {
        return (parent.isDefault()) || !parent.node.hasValue(name);
    }

    @Override
    public boolean hasDescriptor() {
        return parent.hasDescriptor() && parent.getDescriptor().valueDescriptors().containsKey(getName());
    }
    
    /**
     * True if this node is frozen and could not be edited
     * @return 
     */
    @Override
    public boolean isFrozen(){
        return frozen;
    }    

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    

}
