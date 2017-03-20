/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.test;

import hep.dataforge.fx.output.FXOutputPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander Nozik
 */
public class FXOutputPaneTest extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        FXOutputPane out = new FXOutputPane();
        out.setMaxLines(5);

        for (int i = 0; i < 12; i++) {
            out.appendLine("my text number " + i);
        }

//        onComplete.appendLine("a\tb\tc");
//        onComplete.appendLine("aaaaa\tbbb\tccc");

        Scene scene = new Scene(out.getRoot(), 400, 400);

        primaryStage.setTitle("FXOutputPaneTest");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}
