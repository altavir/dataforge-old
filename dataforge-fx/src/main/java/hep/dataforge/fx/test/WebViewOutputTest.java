/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.test;

import hep.dataforge.fx.FXDataOutputPane;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author Alexander Nozik
 */
public class WebViewOutputTest extends Application {

    @Override
    public void start(Stage primaryStage) {

        WebView webView = new WebView();

        webView.getEngine().loadContent("a\tb\tc\naaaaa\tbbb\tccc");
        

        Scene scene = new Scene(webView, 400, 400);

        primaryStage.setTitle("WebViewOutputTest");
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
