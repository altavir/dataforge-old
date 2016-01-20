/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.values.Value;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

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

    /**
     * getter for meta value.
     *
     * @return
     */
    ObservableValue<Value> value();

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
     *
     * @return
     */
    boolean hasDescriptor();

    default ObservableValue<String> nameProperty() {
        //return new ReadOnlyStringWrapper(getName());
        return new StringBinding() {
            @Override
            protected String computeValue() {
                return getName();
            }
        };
    }

    default ObservableValue<String> descriptionProperty() {
        return new StringBinding() {
            @Override
            protected String computeValue() {
                return getDescription();
            }
        };
    }

    default ObservableValue<String> stringValueProperty() {
        return new StringBinding() {
            @Override
            protected String computeValue() {
                if (value() == null) {
                    return "";
                } else {
                    return value().getValue().stringValue();
                }
            }
        };
    }

    /**
     * True if this node is frozen and could not be edited
     *
     * @return
     */
    boolean isFrozen();
    
    /**
     * Shows if node is visible in configurator
     * @return 
     */
    boolean isVisible();
}
