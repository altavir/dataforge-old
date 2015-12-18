/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.values.Value;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;

/**
 * Tree item for meta nodes and values
 *
 * @author Alexander Nozik
 */
public interface MetaTree {

    /**
     * getter for name property
     *
     * @return
     */
    String getName();

//    /**
//     * setter for name. Used only for new nodes
//     */
//    void setName();
    /**
     * getter for meta value.
     *
     * @return
     */
    Value getValue();

    /**
     * setter for value
     *
     * @param value
     */
    void setValue(Value value);

    default void setStringValue(String value) {
        setValue(Value.of(value));
    }

    default String getStringValue() {
        if (getValue() == null) {
            return "";
        } else {
            return getValue().stringValue();
        }
    }

    /**
     * getter for description
     *
     * @return
     */
    String getDescription();

    /**
     * is MetaNode tree item
     *
     * @return
     */
    boolean isNode();

    /**
     * is default value from descriptor
     *
     * @return
     */
    boolean isDefault();
    
    /**
     * true if there is a descriptor for this element
     * @return 
     */
    boolean hasDescriptor();
    
    default StringProperty nameProperty() {
        //return new ReadOnlyStringWrapper(getName());
        return new ReadOnlyStringWrapper(this, "name", getName());
    }

    default StringProperty descriptionProperty() {
        //return new ReadOnlyStringWrapper(getDescription());
        return new ReadOnlyStringWrapper(this, "description", getDescription());
    }

    default StringProperty stringValueProperty() {
        return new SimpleStringProperty(this, "stringValue", getStringValue());
    }

    default ObjectProperty<Value> valueProperty() {
        try {
            return new JavaBeanObjectPropertyBuilder<>().bean(this).name("value").build();
        } catch (NoSuchMethodException ex) {
            throw new Error(ex);
        }
    }
    
    /**
     * True if this node is frozen and could not be edited
     * @return 
     */
    default boolean isFrozen(){
        return false;
    }    
}
