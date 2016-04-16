/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.values.Value;
import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

public class ComboBoxValueChooser extends ValueChooserBase<ComboBox<Value>> {

//    @Override
//    protected void displayError(String error) {
//        //TODO ControlsFX decorator here
//    }
    
    protected Collection<Value> allowedValues(){
        return descriptorProperty().get().allowedValues().keySet();
    }

    @Override
    protected ComboBox<Value> buildNode() {
        ComboBox<Value> node = new ComboBox<>(FXCollections.observableArrayList(allowedValues()));
        node.setMaxWidth(Double.MAX_VALUE);
        node.setEditable(false);
        node.getSelectionModel().select(currentValue());
        this.valueProperty().bind(node.valueProperty());
        return node;
    }

    @Override
    public void setDisplayValue(Value value) {
        getNode().getSelectionModel().select(value);
    }

}
