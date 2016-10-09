/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.fragments;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * A separate window. In future could possibly be attachable
 *
 * @author Alexander Nozik
 */
public class FragmentWindow implements AutoCloseable {

    private final Fragment fragment;
    private Stage stage;
    private Supplier<Window> owner;
    private Runnable onShow;
    private Runnable onHide;

    public FragmentWindow(Fragment fragment) {
        this.fragment = fragment;
        owner = null;
    }

    public FragmentWindow(Fragment fragment, Window owner) {
        this.fragment = fragment;
        this.owner = () -> owner;
    }

    public void setOwner(Window owner) {
        this.owner = () -> owner;
        try {
            stage.initOwner(owner);
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to set window owner", ex);
        }
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

//    /**
//     * Create a window for scene root and set its parameters.
//     * @param root
//     * @return
//     */
//    protected abstract Stage buildStage(Parent root);


    /**
     * Helper method to create stage with given size and title
     *
     * @param root
     * @param title
     * @param width
     * @param height
     * @return
     */
    protected final Stage buildStage(Parent root, String title, double width, double height) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.sizeToScene();

        return stage;
    }

    public Stage getStage() {
        if (stage == null) {
            stage = buildStage(fragment.getRoot(), fragment.getTitle(), fragment.getPreferedWidth(), fragment.getPreferedHeight());
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
            hide();
            stage.close();
            stage = null;
        }
    }

    public void hide() {
        fragment.hide();
        getStage().hide();
    }

    public void show() {
        fragment.show();
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
