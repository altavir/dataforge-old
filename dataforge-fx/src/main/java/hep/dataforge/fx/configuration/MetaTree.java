/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration;

import java.util.Observable;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;

/**
 * Tree item for meta nodes and values
 *
 * @author Alexander Nozik
 */
public abstract class MetaTree extends Observable {

    private final StringBinding nameValue = new StringBinding() {
        @Override
        protected String computeValue() {
            return MetaTree.this.getName();
        }
    };

    private final StringBinding descriptionValue = new StringBinding() {
        @Override
        protected String computeValue() {
            return MetaTree.this.getDescription();
        }
    };

    private final BooleanProperty protectedProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty visibleProperty = new SimpleBooleanProperty(true);

    public BooleanProperty protectedProperty(){
        return protectedProperty;
    }
    
    public BooleanProperty visibleProperty(){
        return visibleProperty;
    }
    
    /**
     * 
     * @return
     */
    public ObservableStringValue nameValue() {
        return nameValue;
    }

    /**
     * getter for name property
     *
     * @return
     */
    public abstract String getName();

    public ObservableStringValue descriptionValue() {
        return descriptionValue;
    }

    /**
     * getter for description
     *
     * @return
     */
    public abstract String getDescription();

    /**
     * is MetaNode tree item
     *
     * @return
     */
    protected abstract boolean isNode();

    /**
     * is default value from descriptor
     *
     * @return
     */
    public abstract ObservableBooleanValue isDefault();


    /**
     * true if there is a descriptor for this element
     *
     * @return
     */
    protected abstract boolean hasDescriptor();
    
    public void invalidate(){
        nameValue.invalidate();
        descriptionValue.invalidate();
    }

}
