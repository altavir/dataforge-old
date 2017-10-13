/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.jfreechart;

import hep.dataforge.io.envelopes.DefaultEnvelopeType;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.XMLMetaType;
import hep.dataforge.plots.PlotFrame;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Alexander Nozik
 */
public class FXPlotUtils {

    public static MenuItem getDFPlotExportMenuItem(Window window, PlotFrame frame) {
        MenuItem dfpExport = new MenuItem("DF...");
        dfpExport.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("DataForge envelope", "*.df"));
            chooser.setTitle("Select file to save plot into");
            File file = chooser.showSaveDialog(window);
            if (file != null) {
                try {
                    new DefaultEnvelopeWriter(DefaultEnvelopeType.instance, XMLMetaType.instance)
                            .write(new FileOutputStream(file), new PlotFrame.Wrapper().wrap(frame));
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to save plot to file", ex);
                }
            }
        });
        return dfpExport;
    }
}
