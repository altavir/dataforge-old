/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.fragments;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

    //TODO allow node and node property instead of fragment

    private final FXFragment fragment;
    private Stage stage;
    private Supplier<Window> owner;
    private BooleanProperty isShowing = new SimpleBooleanProperty(this, "isShowing", false);
    private DoubleProperty xPos = new SimpleDoubleProperty();
    private DoubleProperty yPos = new SimpleDoubleProperty();

    {
        isShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue != getStage().isShowing()) {
                if (newValue) {
                    show();
                } else {
                    hide();
                }
            }
        });
    }

    public FragmentWindow(FXFragment fragment) {
        this.fragment = fragment;
        owner = null;
    }

    public FragmentWindow(FXFragment fragment, Window owner) {
        this.fragment = fragment;
        setOwner(() -> owner);
    }

    private final void setOwner(Supplier<Window> owner) {
        this.owner = owner;
        if (stage != null) {
            try {
                stage.initOwner(owner.get());
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass()).error("Failed to set window owner", ex);
            }
        }
    }

    /**
     * Bind this window showing to some observable value
     *
     * @param boolVal
     */
    public void bindTo(ObservableValue<Boolean> boolVal) {
        isShowing.bind(boolVal);
    }

    public void bindTo(BooleanProperty property) {
        isShowing.bindBidirectional(property);
    }

    /**
     * Bind window to switch
     *
     * @param button
     */
    public void bindTo(ToggleButton button) {
        bindTo(button.selectedProperty());
        setOwner(() -> button.getScene().getWindow());
    }


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
            stage = buildStage(fragment.getFXNode(), fragment.getTitle(), fragment.getWidth(), fragment.getHeight());
            stage.setOnShown((WindowEvent event) -> {
                onShow();
            });

            fragment.heightProperty().bind(stage.heightProperty());
            fragment.widthProperty().bind(stage.widthProperty());
            xPos.bind(stage.xProperty());
            yPos.bind(stage.yProperty());

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
        getStage().hide();
    }

    public void show() {
        Stage stage = getStage();
        stage.show();
        stage.setWidth(fragment.getWidth());
        stage.setHeight(fragment.getHeight());
        stage.setX(xPos.doubleValue());
        stage.setY(yPos.doubleValue());
    }

    protected void onShow() {
        isShowing.set(true);
    }

    protected void onHide() {
        isShowing.set(false);
    }

}
