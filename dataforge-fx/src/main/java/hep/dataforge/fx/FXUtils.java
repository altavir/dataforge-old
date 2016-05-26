/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 *
 * @author Alexander Nozik
 */
public class FXUtils {

    /**
     * Add a listener that performs some update action on any window size change
     *
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

    public static String colorToString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Check if current thread is FX application thread to avoid runLater from
     * UI thread.
     *
     * @param r
     */
    public static void run(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

}
