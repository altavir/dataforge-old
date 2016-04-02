/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.test;

import hep.dataforge.fx.FXDataOutputPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander Nozik
 */
public class FXOutputPaneTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        
        FXDataOutputPane out = new FXDataOutputPane();
        
        out.appendLine("a\tb\tc");
        out.appendLine("aaaaa\tbbb\tccc");
        
        Scene scene = new Scene(out, 400, 400);
        
        primaryStage.setTitle("FXOutputPaneTest");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
