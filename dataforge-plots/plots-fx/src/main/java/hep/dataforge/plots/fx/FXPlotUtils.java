/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.fx;

import hep.dataforge.fx.ApplicationSurrogate;
import hep.dataforge.fx.FXPlugin;
import hep.dataforge.io.envelopes.DefaultEnvelopeType;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.XMLMetaType;
import hep.dataforge.plots.PlotFrame;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Alexander Nozik
 */
public class FXPlotUtils {

    public static void addExportPlotAction(ContextMenu menu, PlotFrame frame) {
        MenuItem exportAction = new MenuItem("Export DFP");
        exportAction.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("DataForge plot", "*.dfp"));
            chooser.setTitle("Select file to save plot into");
            File file = chooser.showSaveDialog(menu.getOwnerWindow());
            if (file != null) {
                try {
                    new DefaultEnvelopeWriter(DefaultEnvelopeType.instance, XMLMetaType.instance).write(new FileOutputStream(file), frame.wrap());
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to save plot to file", ex);
                }
            }
        });
        menu.getItems().add(exportAction);
    }

    /**
     * Display plot container in a separate stage window
     *
     * @param title
     * @param width
     * @param height
     * @return
     */
    public static PlotContainer displayContainer(FXPlugin fx, String title, double width, double height) {
        PlotContainer container = new PlotContainer();
        fx.show(buildPlotStage(container, title, width, height));
        return container;
    }

    /**
     * Display a single plot container without initiation framework and terminating it after window is closed
     *
     * @param title
     * @param width
     * @param height
     * @return
     */
    public static PlotContainer displayContainer(String title, double width, double height) {
        Platform.setImplicitExit(false);
        ApplicationSurrogate.start();
        PlotContainer container = new PlotContainer();
        Platform.runLater(() -> {
            buildPlotStage(container, title, width, height).accept(ApplicationSurrogate.getStage());
            ApplicationSurrogate.getStage().show();
        });

        Platform.setImplicitExit(true);
        return container;
    }

    private static Consumer<Stage> buildPlotStage(PlotContainer container, String title, double width, double height) {
        return stage -> {
            stage.setWidth(width);
            stage.setHeight(height);
            Scene scene = new Scene(container.getPane(), width, height);
            stage.setTitle(title);
            stage.setScene(scene);
        };
    }
}
