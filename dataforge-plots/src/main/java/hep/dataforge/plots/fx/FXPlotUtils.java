/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.fx;


import hep.dataforge.fx.RootApplication;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander Nozik
 */
public class FXPlotUtils {

    /**
     * Display plot container in a separate stage window
     *
     * @param titile
     * @param width
     * @param height
     * @return
     */
    public static PlotContainer displayContainer(String titile, double width, double height) {
        PlotContainerHolder containerHolder = new PlotContainerHolder();

        RootApplication.show(() -> {
            Stage stage = new Stage();
            stage.setWidth(width);
            stage.setHeight(height);
            PlotContainer container = new PlotContainer();
            containerHolder.setContainer(container);
            Scene scene = new Scene(container, width, height);
            stage.setTitle(titile);
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
        JFreeChartFrame frame = new JFreeChartFrame(title, meta);
        container.setPlot(frame);
        return frame;
    }

    public static JFreeChartFrame displayJFreeChart(String title, Meta meta) {
        return displayJFreeChart(title, 800, 600, meta);
    }
    
    private static class PlotContainerHolder{
        private PlotContainer container;

        public synchronized PlotContainer getContainer() throws InterruptedException {
            while(container == null){
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
