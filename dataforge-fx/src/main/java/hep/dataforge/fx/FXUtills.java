/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

/**
 *
 * @author Alexander Nozik
 */
public class FXUtills {

    /**
     * Add a listener that performs some update action on any window size change
     * @param component
     * @param action 
     */
    public static void addWindowResizeListener(Region component, Runnable action) {
        component.widthProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    action.run();
                });
        component.heightProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    action.run();
                });

    }
}