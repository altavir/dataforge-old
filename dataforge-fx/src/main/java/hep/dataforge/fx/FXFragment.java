/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import java.util.function.Supplier;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

/**
 * A separate window. In future could possibly be attachable
 *
 * @author Alexander Nozik
 */
public abstract class FXFragment implements AutoCloseable {

    private Stage stage;
    private Supplier<Window> owner;
    private Runnable onShow;
    private Runnable onHide;

    public FXFragment() {
        owner = null;
    }

    public FXFragment(Window window) {
        this.owner = () -> window;
    }

    public void setOwner(Window owner) {
        this.owner = () -> owner;
    }

    /**
     * Bind this window showing to some observable value
     *
     * @param boolVal
     */
    public void bindTo(ObservableBooleanValue boolVal) {
        boolVal.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue && newValue != getStage().isShowing()) {
                show();
            } else {
                hide();
            }
        });
    }

    /**
     * Bind window to switch
     *
     * @param button
     */
    public void bindTo(ToggleButton button) {
        bindTo(button.selectedProperty());
        owner = () -> button.getScene().getWindow();
        onHide = () -> {
            if (button.isSelected()) {
                button.setSelected(false);
            }
        };
    }

    protected abstract Stage buildStage();

    public Stage getStage() {
        if (stage == null) {
            stage = buildStage();
            stage.setOnShown((WindowEvent event) -> {
                onShow();
            });

            stage.setOnHidden((WindowEvent event) -> {
                onHide();
            });
            if (owner != null) {
                try {
                    stage.initOwner(owner.get());
                } catch (Exception ex) {
                    LoggerFactory.getLogger(getClass()).error("Failed to set window owner", ex);
                }
            }
        }
        return stage;
    }

    /**
     * hide and destroy the stage
     */
    @Override
    public void close() {
        if (stage != null) {
            stage.close();
            stage = null;
        }
    }

    public void hide() {
        getStage().hide();
    }

    public void show() {
        getStage().show();
    }

    protected void onShow() {
        if (onShow != null) {
            onShow.run();
        }
    }

    protected void onHide() {
        if (onHide != null) {
            onHide.run();
        }
    }

}
