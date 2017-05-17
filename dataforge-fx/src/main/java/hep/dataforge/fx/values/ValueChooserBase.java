/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.description.ValueDescriptor;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.slf4j.LoggerFactory;

/**
 * ValueChooser boilerplate
 *
 * @author Alexander Nozik
 */
public abstract class ValueChooserBase<T extends Node> implements ValueChooser {

    private ValueCallback callback;
    private T node;
    private final ObjectProperty<Value> valueProperty = new SimpleObjectProperty<>(Value.NULL);
    private final ObjectProperty<ValueDescriptor> descriptorProperty = new SimpleObjectProperty<>();
    private final BooleanProperty showDefaultProperty = new SimpleBooleanProperty(false);

    @Override
    public void setValueCallback(ValueCallback callback) {
        this.callback = callback;
        valueProperty.addListener((ObservableValue<? extends Value> observable, Value oldValue, Value newValue) -> {
            if (callback != null) {
                ValueCallbackResponse response = callback.update(newValue);
                if (response.value != null && !response.value.equals(valueProperty.get())) {
                    setDisplayValue(response.value);
                }

                if (!response.success) {
                    displayError(response.message);
                }
            }
        });
    }

    @Override
    public ValueCallback getValueCallback() {
        //PENDING add dummy cllback here?
        return this.callback;
    }

    @Override
    public ObjectProperty<Value> valueProperty() {
        return valueProperty;
    }

    @Override
    public ObjectProperty<ValueDescriptor> descriptorProperty() {
        return descriptorProperty;
    }

    @Override
    public BooleanProperty showDefaultProperty() {
        return showDefaultProperty;
    }

    @Override
    public T getNode() {
        if (this.node == null) {
            this.node = buildNode();
        }
        return node;
    }
    
    public void resetValue(){
        setDisplayValue(currentValue());
    }

    /**
     * Current value or default value
     * @return 
     */
    protected Value currentValue() {
        Value value = valueProperty().get();
        if (value == null || value.valueType() == ValueType.NULL) {
            ValueDescriptor descriptor = getDescriptor();
            if (descriptor != null) {
                return descriptor.defaultValue();
            } else {
                return Value.NULL;
            }
        } else {
            return value;
        }
    }

    /**
     * True if build node is successful
     *
     * @return
     */
    protected abstract T buildNode();

    /**
     * Display validation error
     *
     * @param error
     */
    protected void displayError(String error) {
        LoggerFactory.getLogger(getClass()).error(error);
    }

}
