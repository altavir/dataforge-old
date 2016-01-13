/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.values.Value;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

public class ComboBoxValueChooser implements ValueChooser {

    private List<Value> allowedValues;
    private Value defaultValue;
    private ComboBox<Value> node;
    private ValueCallback callback;

    @Override
    public Node getNode() {
        if (node == null) {
            node = new ComboBox<>(FXCollections.observableArrayList(allowedValues));
            node.set
            node.setEditable(false);
            applyCallback();
        }
        return node;
    }

    private void applyCallback() {
        if (node != null && callback != null) {
            node.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Value>() {
                @Override
                public void changed(ObservableValue<? extends Value> observable, Value oldValue, Value newValue) {
                    ValueCallbackResponse response = callback.update(newValue);
                    //FIXME add error message and validator here
                }
            });
        }
    }

    @Override
    public void setValueCallback(ValueCallback callback) {
        this.callback = callback;
        applyCallback();
    }

    @Override
    public void updateValue(Value value) {
        node.set
    }

}
