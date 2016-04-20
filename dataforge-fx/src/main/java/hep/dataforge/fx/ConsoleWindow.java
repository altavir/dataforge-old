/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import de.jensd.shichimifx.utils.ConsoleDude;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Alexander Nozik
 */
public class ConsoleWindow {

    private Stage stage;
    private TextArea consolePane;
    

    public ConsoleWindow(ToggleButton button) {
        consolePane = new TextArea();
        consolePane.setEditable(false);
        consolePane.setWrapText(true);

        stage = new Stage();
        stage.setTitle("Vacuum measurement console");
        stage.setScene(new Scene(consolePane, 800, 200));
        stage.setOnHidden((WindowEvent event) -> {
            button.setSelected(false);
        });
        button.setOnAction((ActionEvent event) -> {
            if (button.isSelected() && button.isSelected() != stage.isShowing()) {
                stage.show();
                onShow(consolePane);
            } else {
                stage.hide();
                onHide(consolePane);
            }
        });
        consolePane.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            //FIXME fix this
            if(newValue.length()> 2000){
                consolePane.clear();
            }
        });
    }

    public Stage getStage() {
        return stage;
    }

    public void appendText(String text) {
        consolePane.appendText(text);
    }

    public void appendLine(String text) {
        consolePane.appendText(text + "\r\n");
    }

    protected void onShow(TextArea textArea) {

    }

    protected void onHide(TextArea textArea) {

    }

    public void hookStd() {
        ConsoleDude.hookStdStreams(consolePane);
    }

    public void restoreStd() {
        ConsoleDude.restoreStdStreams();
    }

}
