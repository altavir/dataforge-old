/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.description.ValueDescriptor;
import hep.dataforge.values.Value;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.Node;

/**
 * A value chooser object. Must have an empty constructor to be invoked by
 * reflections.
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public interface ValueChooser {

    //TODO replace by fx bindings
    void setValueCallback(ValueCallback callback);

    ValueCallback getValueCallback();

    ObjectProperty<Value> valueProperty();

    /**
     * The descriptor property for this value. Could be null
     *
     * @return
     */
    ObjectProperty<ValueDescriptor> descriptorProperty();

    /**
     * If showDefault, than default Value from descriptor is shown (if found)
     *
     * @return
     */
    BooleanProperty showDefaultProperty();

    /**
     * Set display value but do not notify listeners
     *
     * @param value
     */
    void setDisplayValue(Value value);

    /**
     * Set value and notify callback if it is present
     *
     * @param value
     */
    default void setValue(Value value) {
        valueProperty().set(value);
    }

    default void bindValue(ObservableObjectValue<Value> value) {
        valueProperty().bind(value);
    }

    default void setDescriptor(ValueDescriptor desc) {
        descriptorProperty().set(desc);
    }

    default void setShowDefault(boolean showDefault) {
        showDefaultProperty().set(showDefault);
    }

    default void setDisabled(boolean disabled) {
        //TODO replace by property
    }

    default ValueDescriptor getDescriptor() {
        return descriptorProperty().get();
    }

    /**
     * Get or create a Node that could be later inserted into some parent
     * object.
     *
     * @return
     */
    Node getNode();

}
