/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.viewer;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander Nozik
 */
public class PlotViewer extends Application {
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlotViewer.fxml"));
        Parent root = loader.load();
        PlotViewerController controller = loader.getController();
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("DataForge plot viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        for(String fileName: this.getParameters().getUnnamed()){
            controller.loadPlot(new File(fileName));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
