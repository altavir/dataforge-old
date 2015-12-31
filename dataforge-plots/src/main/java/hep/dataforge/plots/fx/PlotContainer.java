/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.fx;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.fx.MetaEditor;
import hep.dataforge.meta.Configuration;
import hep.dataforge.plots.PlotFrame;
import hep.dataforge.plots.Plottable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
public class PlotContainer extends AnchorPane implements Initializable {

    public static PlotContainer anchorTo(AnchorPane pane) {
        PlotContainer container = new PlotContainer();
        pane.getChildren().add(container);
        AnchorPane.setBottomAnchor(container, 0d);
        AnchorPane.setTopAnchor(container, 0d);
        AnchorPane.setLeftAnchor(container, 0d);
        AnchorPane.setRightAnchor(container, 0d);
        return container;
    }

    @FXML
    private AnchorPane root;
    @FXML
    private AnchorPane plotPane;
    @FXML
    private AnchorPane specialOptionsPane;
    @FXML
    private ListView<Plottable> plottableslList;
    @FXML
    private Button optinosPannelButton;
    @FXML
    private Button frameOptionsButton;
    @FXML
    private SplitPane split;

    private PlotFrame plot;

    private Map<Configuration, Stage> configWindows = new HashMap<>();

    public PlotContainer() {
        FXMLLoader loader = new FXMLLoader(MetaEditor.class.getResource("/fxml/PlotContainer.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (Exception ex) {
            LoggerFactory.getLogger("FX").error("Error during fxml initialization", ex);
            throw new Error(ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        plottableslList.setCellFactory((ListView<Plottable> param) -> new PlottableListCell());
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    }

    /**
     * An AncorPane holding any special options for this plot displayed atop of
     * plottables list
     *
     * @return
     */
    public AnchorPane getSpecialOptionsPane() {
        return specialOptionsPane;
    }

//    public void setPlot
    public PlotFrame getPlot() {
        return plot;
    }

    public void setPlot(PlotFrame plot) {
        this.plot = plot;
        plotPane.getChildren().retainAll(optinosPannelButton);
        this.plot.display(plotPane);
        plottableslList.setItems(plot.getAll());
    }

    protected void setupOptions() {
        plottableslList.getItems().clear();
    }

    /**
     * Display configurator in separate scene
     *
     * @param config
     * @param desc
     */
    private void displayConfigurator(String header, Configuration config, NodeDescriptor desc) {
        Stage stage;
        if (configWindows.containsKey(config)) {
            stage = configWindows.get(config);
        } else {
            stage = new Stage();
            Scene scene = new Scene(MetaEditor.build(config, desc));
            stage.setScene(scene);
            stage.setHeight(400);
            stage.setWidth(400);
            stage.setTitle(header);
            stage.setOnCloseRequest((WindowEvent event) -> {
                configWindows.remove(config);
            });
            stage.initOwner(getScene().getWindow());
            configWindows.put(config, stage);
        }
        stage.show();
        stage.toFront();
    }

    @FXML
    private void onFrameOptionsClick(ActionEvent event) {
        if (plot != null) {
            displayConfigurator("Plot frame configuration: " + plot.getName(),
                    plot.getConfig(), DescriptorUtils.buildDescriptor(plot));
        }
    }

    private class PlottableListCell extends ListCell<Plottable> {

        private HBox content;
        private CheckBox title;
        private Button configButton;

        @Override
        protected void updateItem(Plottable item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                clearContent();
            } else {
                setContent(item);
            }
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void setContent(Plottable item) {
            setText(null);
            
            title = new CheckBox();
            title.setSelected(true);
            configButton = new Button("...");
            configButton.setMinWidth(0);
            Pane space = new Pane();
            HBox.setHgrow(space, Priority.ALWAYS);
            content = new HBox(title, space, configButton);
            HBox.setHgrow(content, Priority.ALWAYS);
            content.setMaxWidth(Double.MAX_VALUE);            
            Configuration config = item.getConfig();

            title.setText(config.getString("title", item.getName()));
            title.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                config.setValue("visible", newValue);
            });

            if (config.hasValue("color")) {
                title.setTextFill(Paint.valueOf(config.getString("color")));
            }

            configButton.setOnAction((ActionEvent event) -> {
                displayConfigurator(item.getName() + " configuration", config, DescriptorUtils.buildDescriptor(item));
            });
            Platform.runLater(() -> setGraphic(content));

        }

    }

}
