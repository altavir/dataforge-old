/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.fx;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.fx.FXUtils;
import hep.dataforge.fx.configuration.MetaEditor;
import hep.dataforge.meta.ConfigChangeListener;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.Plottable;
import hep.dataforge.values.Value;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
public class PlotContainer implements Initializable {

    private AnchorPane root;
    @FXML
    private BorderPane plotPane;
    @FXML
    private VBox sideBar;
    @FXML
    private ListView<Plottable> plottableslList;
    @FXML
    private Button optionsPannelButton;
    @FXML
    private Button frameOptionsButton;
    @FXML
    private SplitPane split;

    private FXPlotFrame plot;
    private Map<Configuration, Stage> configWindows = new HashMap<>();
    private BooleanProperty sidebarVisibleProperty = new SimpleBooleanProperty(true);
    private double lastDividerPosition = -1;

    public PlotContainer() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlotContainer.fxml"));
        root = new AnchorPane();
        loader.setRoot(root);
        loader.setController(this);

        try {
            loader.load();
        } catch (Exception ex) {
            LoggerFactory.getLogger("FX").error("Error during fxml initialization", ex);
            throw new Error(ex);
        }
    }

    public static PlotContainer anchorTo(AnchorPane pane) {
        PlotContainer container = new PlotContainer();
        pane.getChildren().add(container.getRoot());
        AnchorPane.setBottomAnchor(container.getRoot(), 0d);
        AnchorPane.setTopAnchor(container.getRoot(), 0d);
        AnchorPane.setLeftAnchor(container.getRoot(), 0d);
        AnchorPane.setRightAnchor(container.getRoot(), 0d);
        return container;
    }

    /**
     * The root node of container
     *
     * @return
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        plottableslList.setCellFactory((ListView<Plottable> param) -> new PlottableListCell());
        plottableslList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        split.getDividers().get(0).positionProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    sidebarVisibleProperty.set(newValue.doubleValue() < 0.99);
                    if (newValue.doubleValue() < 0.98) {
                        lastDividerPosition = newValue.doubleValue();
                    }
                });

        sidebarVisibleProperty.addListener(
                (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (newValue) {
                        optionsPannelButton.setText(">>");
                    } else {
                        optionsPannelButton.setText("<<");
                    }
                });

        optionsPannelButton.setOnAction((ActionEvent event) -> {
            if (sidebarVisibleProperty.get()) {
                split.setDividerPosition(0, 1d);
            } else if (lastDividerPosition > 0) {
                split.setDividerPosition(0, lastDividerPosition);
            } else {
                split.setDividerPosition(0, 0.8);
            }
        });
        split.setDividerPositions(1.0);
    }

    public void setSideBarExpanded(boolean expanded) {
        this.sidebarVisibleProperty.set(expanded);
    }

    public void setSideBarPosition(double position) {
        split.setDividerPositions(position);
    }

    /**
     * A list of getPlottables in the sidebar
     *
     * @return
     */
    public ListView<Plottable> getPlottableslListView() {
        return plottableslList;
    }

    /**
     * An sideBar VBox
     *
     * @return
     */
    protected VBox getSideBar() {
        return sideBar;
    }

    /**
     * Add nodes to the end of the sideBar
     *
     * @param nodes
     */
    public void addToSideBar(Node... nodes) {
        getSideBar().getChildren().addAll(nodes);
    }

    /**
     * Insert nodes in sideBar at specific index
     *
     * @param index
     * @param nodes
     */
    public void addToSideBar(int index, Node... nodes) {
        getSideBar().getChildren().addAll(index, Arrays.asList(nodes));
    }

    public FXPlotFrame getPlot() {
        return plot;
    }

    /**
     * Set plot to display in this container
     *
     * @param plot
     */
    public void setPlot(FXPlotFrame plot) {
        this.plot = plot;
        FXUtils.runNow(() -> {
            plotPane.getChildren().retainAll(optionsPannelButton);
            plotPane.setCenter(plot.getRoot());

            ListBinding<Plottable> list = new ListBinding<Plottable>() {
                {
                    super.bind(plot);
                }

                @Override
                protected ObservableList<Plottable> computeValue() {
                    return FXCollections.observableList(StreamSupport.stream(plot.spliterator(),false).collect(Collectors.toList()));
                }

            };

            plottableslList.setItems(list);
        });
    }

    /**
     * remove plot from container
     */
    public void removePlot() {
        this.plot = null;
        FXUtils.runNow(() -> {
            plotPane.getChildren().retainAll(optionsPannelButton);
            plottableslList.setItems(FXCollections.emptyObservableList());
        });
    }

    protected void setupSideBar() {
        FXUtils.runNow(() -> plottableslList.getItems().clear());
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
            stage.setOnCloseRequest((WindowEvent event) -> configWindows.remove(config));
            stage.initOwner(root.getScene().getWindow());
            configWindows.put(config, stage);
        }
        stage.show();
        stage.toFront();
    }

    @FXML
    private void onFrameOptionsClick(ActionEvent event) {
        if (plot != null) {
            displayConfigurator("Plot frame configuration",
                    plot.getConfig(), DescriptorUtils.buildDescriptor(plot));
        }
    }

    @FXML
    private void onShowAll(ActionEvent event) {
        this.plot.forEach(pl -> pl.configureValue("visible", true));
    }

    @FXML
    private void onHideAll(ActionEvent event) {
        this.plot.forEach(pl -> pl.configureValue("visible", false));
    }

    protected class PlottableListCell extends ListCell<Plottable> implements ConfigChangeListener {

        private HBox content;
        private CheckBox title;
        private Button configButton;

        /**
         * Configuration to which this cell is bound
         */
        private Configuration config;

        @Override
        protected synchronized void updateItem(Plottable item, boolean empty) {
            super.updateItem(item, empty);

            //cleaning up after item with different config
            if (config != null) {
                config.removeObserver(this);
            }

            if (empty) {
                clearContent();
            } else {
                config = item.getConfig();
                config.addObserver(this, false);
                setContent(item);
            }
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private synchronized void setContent(Plottable item) {
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

//                title.textProperty().bindBidirectional(new ConfigStringValueProperty(config, "title"));
            title.setText(config.getString("title", item.getName()));
            title.setSelected(config.getBoolean("visible", true));
//                title.selectedProperty().bindBidirectional(new ConfigBooleanValueProperty(config, "visible"));
            title.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                config.setValue("visible", newValue);
            });

            if (config.hasValue("color")) {
                title.setTextFill(Color.valueOf(config.getString("color")));
            }

            configButton.setOnAction((ActionEvent event) -> {
                displayConfigurator(item.getName() + " configuration", config, DescriptorUtils.buildDescriptor(item));
            });
            setGraphic(content);
        }

        @Override
        public synchronized void notifyValueChanged(String name, Value oldItem, Value newItem) {
            FXUtils.runNow(() -> {
                switch (name) {
                    case "title":
                        title.setText(newItem.stringValue());
                        break;
                    case "color":
                        title.setTextFill(Color.valueOf(newItem.stringValue()));
                        break;
                    case "visible":
                        title.setSelected(newItem.booleanValue());
                        break;
                    default:
                        break;
                }
            });
        }

        @Override
        public void notifyElementChanged(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem) {
            //ignore
        }

    }

}
