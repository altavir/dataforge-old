/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.description.ValueDescriptor;
import hep.dataforge.values.Value;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class TextValueChooser extends ValueChooserBase<TextField> {

    @Override
    protected TextField buildNode() {
        TextField node = new TextField();
        Value defaultValue = currentValue();
        if (defaultValue != null) {
            node.setText(currentValue().stringValue());
            node.setStyle(String.format("-fx-text-fill: %s;", textColor(defaultValue)));
        }
        // commit on enter
        node.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                commit();
            }
        });
        // restoring value on click outside
        node.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (oldValue && !newValue) {
                node.setText(currentValue().stringValue());
            }
        });

        // changing text color while editing
        node.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            Value value = Value.of(newValue);
            if (!validate(value)) {
                getNode().setStyle(String.format("-fx-text-fill: %s;", "red"));
            } else {
                getNode().setStyle(String.format("-fx-text-fill: %s;", textColor(value)));
            }
        });

        return node;
    }

    protected void commit() {
        Value val = Value.of(getNode().getText());
        if (validate(val)) {
            valueProperty().set(val);
        } else {
            resetValue();
            displayError("Value not allowed");
        }

    }

    protected String textColor(Value item) {
        switch (item.getType()) {
            case BOOLEAN:
                if (item.booleanValue()) {
                    return "blue";
                } else {
                    return "salmon";
                }
            case STRING:
                return "brown";
            default:
                return "black";
        }
    }

    protected boolean validate(Value val) {
        ValueDescriptor descriptor = this.descriptorProperty().get();
        return descriptor == null || descriptor.isValueAllowed(val);
    }

//    @Override
//    protected void displayError(String error) {
//        //TODO ControlsFX decorator here
//    }

    @Override
    public void setDisplayValue(Value value) {
        getNode().setText(value.stringValue());
    }
}
