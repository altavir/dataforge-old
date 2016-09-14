/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.viewer;

import hep.dataforge.io.envelopes.DefaultEnvelopeReader;
import hep.dataforge.plots.fx.FXPlotFrame;
import hep.dataforge.plots.fx.PlotContainer;
import hep.dataforge.plots.wrapper.PlotUnWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for PlotViewer
 *
 * @author Alexander Nozik
 */
public class PlotViewerController implements Initializable {

    private final Map<File, PlotContainer> plotMap = new HashMap<>();
    @FXML
    private Button loadButton;
    @FXML
    private TabPane tabs;

    public void loadPlot(File file) throws IOException {
        PlotContainer container;
        if (plotMap.containsKey(file)) {
            container = plotMap.get(file);
        } else {
            AnchorPane pane = new AnchorPane();
            Tab tab = new Tab(file.getName(), pane);
            container = PlotContainer.anchorTo(pane);
            plotMap.put(file, container);
            tab.setOnClosed(event -> plotMap.remove(file));
            tabs.getTabs().add(tab);
        }
        container.setPlot((FXPlotFrame) new PlotUnWrapper().unWrap(new DefaultEnvelopeReader().read(new FileInputStream(file))));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select plot file to load");
            chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("DataForge plot", "*.dfp"));
            List<File> list = chooser.showOpenMultipleDialog(loadButton.getScene().getWindow());
            list.stream().forEach((f) -> {
                try {
                    loadPlot(f);
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass()).error("Failed to load dfp file", ex);
                }
            });
        });
    }

}
