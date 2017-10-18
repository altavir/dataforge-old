/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.plots;

import hep.dataforge.context.Context;
import hep.dataforge.io.envelopes.DefaultEnvelopeType;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.XMLMetaType;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.DataPlot;
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.XYAdapter;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author darksnake
 */
public class PlotUtils {

    public static Color getAWTColor(Meta reader, Color def) {
        if (reader.hasValue("color")) {
            javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.valueOf(reader.getString("color"));
            return new Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue());
        } else {
            return def;
        }
    }

    public static String awtColorToString(Color color) {
        javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                color.getTransparency()
        );
        return String.format("#%02X%02X%02X",
                (int) (fxColor.getRed() * 255),
                (int) (fxColor.getGreen() * 255),
                (int) (fxColor.getBlue() * 255));
    }

    public static double getThickness(Meta reader) {
        return reader.getDouble("thickness", -1);
    }

    /**
     * Строка для отображениея в легенде
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getTitle(Meta reader) {
        return reader.getString("title", "");
    }

    public static void setXAxis(PlotFrame frame, String title, String units, String type) {
        MetaBuilder builder = new MetaBuilder("xAxis")
                .setValue("axisTitle", title)
                .setValue("axisUnits", units)
                .setValue("type", type);
        frame.getConfig().setNode(builder);
    }

    public static void setYAxis(PlotFrame frame, String title, String units, String type) {
        MetaBuilder builder = new MetaBuilder("yAxis")
                .setValue("axisTitle", title)
                .setValue("axisUnits", units)
                .setValue("type", type);
        frame.getConfig().setNode(builder);
    }

    public static void setTitle(PlotFrame frame, String title) {
        frame.configureValue("title", title);
    }

    public static PlotPlugin getPlotManager(Context context) {
        return context.getFeature(PlotPlugin.class);
    }

    /**
     * TODO move from plots module to implementations
     *
     * @param window
     * @param frame
     * @return
     */
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

    @NotNull
    public static Table extractData(DataPlot plot, Meta query) {
        XYAdapter adapter = plot.getAdapter();
        return new ListTable(adapter.getFormat(), plot.getData(query));

    }
}
