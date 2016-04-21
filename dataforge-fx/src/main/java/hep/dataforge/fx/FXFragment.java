/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * A separate window. In future could possibly be attachable
 *
 * @author Alexander Nozik
 */
public abstract class FXFragment implements AutoCloseable {

    private Stage stage;
    private final Window owner;

    public FXFragment() {
        owner = null;
    }

    public FXFragment(Window owner) {
        this.owner = owner;
    }

    /**
     * Bind this window showing to some observable value
     *
     * @param boolVal
     */
    public void bindTo(ObservableBooleanValue boolVal) {
        boolVal.addListener(new WeakChangeListener<>(
                (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (newValue && newValue != stage.isShowing()) {
                        stage.show();
                    } else {
                        stage.hide();
                    }
                }));
    }

    /**
     * Bind window to switch
     *
     * @param button
     */
    public void bindTo(ToggleButton button) {
        bindTo(button.selectedProperty());
        stage.setOnHidden((WindowEvent event) -> {
            if (button.isSelected()) {
                button.setSelected(false);
            }
        });
    }

    protected abstract Stage buildStage();

    public Stage getStage() {
        if (stage == null) {
            stage = buildStage();
            stage.setOnShowing((WindowEvent event) -> {
                onShow();
            });

            stage.setOnHidden((WindowEvent event) -> {
                onHide();
            });
            if (owner != null) {
                stage.initOwner(owner);
            }
        }
        return stage;
    }
    
    /**
     * hide and destroy the stage
     */
    @Override
    public void close(){
        stage.close();
        stage = null;
    }

    public void hide() {
        getStage().hide();
    }

    public void show() {
        getStage().show();
    }

    protected void onShow() {

    }

    protected void onHide() {

    }

}
