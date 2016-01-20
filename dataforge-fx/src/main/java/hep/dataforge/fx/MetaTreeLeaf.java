/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.description.ValueDescriptor;
import hep.dataforge.fx.values.ValueChooser;
import hep.dataforge.fx.values.ValueChooserFactory;
import hep.dataforge.meta.Configuration;
import hep.dataforge.values.Value;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;

public class MetaTreeLeaf implements MetaTree {

    MetaTreeBranch parent;
    
    String valueName;

    public MetaTreeLeaf(MetaTreeBranch parent, String name) {
        this.parent = parent;
        this.valueName = name;
    }

    @Override
    public String getName() {
        return valueName;
    }

    private Value getValue() {
        if (parent.getNode() != null && parent.getNode().hasValue(valueName)) {
            return parent.node.getValue(valueName);
        } else if (parent.getDescriptor() != null) {
            ValueDescriptor vd = parent.descriptor.valueDescriptor(valueName);
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
    public ObservableValue<Value> value() {
        return new ObjectBinding<Value>() {
            @Override
            protected Value computeValue() {
                return MetaTreeLeaf.this.getValue();
            }
        };
    }

    @Override
    public String getDescription() {
        if (parent.getDescriptor() == null) {
            return null;
        } else {
            ValueDescriptor vd = parent.getDescriptor().valueDescriptor(valueName);
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
            return parent.getDescriptor().valueDescriptor(valueName);
        }
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isDefault() {
        return (parent.isDefault()) || !parent.node.hasValue(valueName);
    }

    @Override
    public boolean hasDescriptor() {
        return parent.hasDescriptor() && parent.getDescriptor().valueDescriptors().containsKey(getName());
    }

    public Configuration getParentNode() {
        return parent.getNode();
    }

    public ValueChooser valueChooser() {
        ValueChooser chooser;
        if (hasDescriptor()) {
            chooser = ValueChooserFactory.getInstance().build(getDescriptor(), getParentNode(), valueName);
        } else {
            chooser = ValueChooserFactory.getInstance().build(getParentNode(), valueName);
        }
        chooser.setDisabled(isFrozen());
        return chooser;
    }
    
    @Override
    public boolean isFrozen() {
        return !hasDescriptor() || getDescriptor().meta().getBoolean("editor.frozen", false);
    }

    @Override
    public boolean isVisible() {
        return !hasDescriptor() || getDescriptor().meta().getBoolean("editor.visible", true);
    }    

}
