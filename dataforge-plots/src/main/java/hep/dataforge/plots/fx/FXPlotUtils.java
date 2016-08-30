/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.fx;

import hep.dataforge.fx.RootApplication;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.PlotFrame;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Alexander Nozik
 */
public class FXPlotUtils {

    public static void addExportPlotAction(ContextMenu menu, PlotFrame frame) {
        MenuItem exportAction = new MenuItem("Export DFP");
        exportAction.setOnAction(event->{
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("DataForge plot", "*.dfp"));
            chooser.setTitle("Select file to save plot into");
            File file = chooser.showSaveDialog(menu.getOwnerWindow());
            if(file != null){
                try {
                    DefaultEnvelopeWriter.instance.write(new FileOutputStream(file), frame.wrap());
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
    public static PlotContainer displayContainer(String title, double width, double height) {
        PlotContainerHolder containerHolder = new PlotContainerHolder();

        RootApplication.show(() -> {
            Stage stage = new Stage();
            stage.setWidth(width);
            stage.setHeight(height);
            PlotContainer container = new PlotContainer();
            containerHolder.setContainer(container);
            Scene scene = new Scene(container.getRoot(), width, height);
            stage.setTitle(title);
            stage.setScene(scene);
            return stage;
        });
        try {
            return containerHolder.getContainer();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Can't get plot container", ex);
        }
    }

    /**
     * Display a JFreeChart plot frame in a separate stage window
     *
     * @param title
     * @param width
     * @param height
     * @return
     */
    public static JFreeChartFrame displayJFreeChart(String title, double width, double height, Meta meta) {
        PlotContainer container = displayContainer(title, width, height);
        JFreeChartFrame frame = new JFreeChartFrame(meta);
        frame.configureValue("title", title);
        container.setPlot(frame);
        return frame;
    }

    public static JFreeChartFrame displayJFreeChart(String title, Meta meta) {
        return displayJFreeChart(title, 800, 600, meta);
    }

    private static class PlotContainerHolder {

        private PlotContainer container;

        public synchronized PlotContainer getContainer() throws InterruptedException {
            while (container == null) {
                wait();
            }
            return container;
        }

        public synchronized void setContainer(PlotContainer container) {
            this.container = container;
            notify();
        }
    }

}
