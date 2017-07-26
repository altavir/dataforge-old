/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.works;

import hep.dataforge.fx.FXUtils;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
public class WorkViewController implements Initializable {

    private Work process;
    @FXML
    private HBox processBox;
    @FXML
    private Label processTitle;
    @FXML
    private Button cancelButton;
    @FXML
    private StackPane progresssPane;
    @FXML
    private ProgressBar progressIndicator;
    @FXML
    private Label progressLabel;

    public static Parent build(Work proc) {
        try {
            FXMLLoader loader = new FXMLLoader(proc.getClass().getResource("/fxml/ProcessView.fxml"));
            Parent p = loader.load();
            WorkViewController controller = loader.getController();
            controller.setProcess(proc);
            return p;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public Work getProcess() {
        return process;
    }

    public void setProcess(@NotNull Work process) {
        this.process = process;
        Platform.runLater(() -> {
            processTitle.setText(process.getTitle());
        });

        this.process.titleProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            FXUtils.runNow(() -> processTitle.setText(newValue));
        });

        updateProgress(process);

        process.progressProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateProgress(process);
        });
        process.maxProgressProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateProgress(process);
        });

        if (process.isDone()) {
            onDone();
        }
        process.isDoneProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                onDone();
            }
        });
        cancelButton.setOnAction(event -> {
            this.process.cancel(true);
        });

    }

    private void updateProgress(Work process) {
        Platform.runLater(() -> {
            double progress = process.getProgress();
            double maxProgress = process.getMaxProgress();
            if (progress > 0) {
                progressLabel.setText(String.format("%d/%d", (int) progress, (int) maxProgress));
                progressIndicator.setProgress(progress / maxProgress);
            } else {
                progressLabel.setText("");
                progressIndicator.setProgress(-1d);

            }
        });
    }

    private void onDone() {
        Platform.runLater(() -> {
            cancelButton.setDisable(true);
            processTitle.setTextFill(Color.GREY);
            progressLabel.setText("COMPLETE");
            progressIndicator.setProgress(1d);
        });
    }

}