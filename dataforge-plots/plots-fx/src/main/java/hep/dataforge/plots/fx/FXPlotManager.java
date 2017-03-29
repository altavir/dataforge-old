/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.fx;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.PluginDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.fx.FXPlugin;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.PlotFrame;
import hep.dataforge.plots.PlotManager;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * A plot holder using MDI-style JavaFX containers
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "plots-fx", group = "hep.dataforge", dependsOn = {"hep.dataforge:fx"},description = "Basic plottiong plugin")
public class FXPlotManager extends BasicPlugin implements PlotManager {
    public static final String FX_FRAME_TYPE_KEY = "fxFrame.type";

    private static ServiceLoader<FXPlotFrameFactory> fxPlotFrameFactoryServiceLoader = ServiceLoader.load(FXPlotFrameFactory.class);

    private final Map<String, PlotContainer> containers = new HashMap<>();

    public FXPlotManager() {
        super.configureValue(FX_FRAME_TYPE_KEY,"default");
    }

    /**
     * Build an FX frame of the given type using spi.
     *
     * @return
     */
    public static FXPlotFrame buildFXPlotFrame(Meta meta) {
        String type = meta.getString(FX_FRAME_TYPE_KEY);
        return StreamSupport.stream(fxPlotFrameFactoryServiceLoader.spliterator(), false)
                .filter(it -> it.getName().equals(type)).findFirst().orElseThrow(() -> new NameNotFoundException(type))
                .build(meta);
    }

    protected FXPlotFrame buildFrame(Meta meta) {
        return buildFXPlotFrame(new Laminate(meta, meta()));
    }

    protected synchronized PlotContainer showPlot(String name, FXPlotFrame frame) {
        FXPlugin fx = getContext().getPlugin(FXPlugin.class);
        PlotContainer container = FXPlotUtils.displayContainer(fx, name, 800, 600);
        container.setPlot(frame);
        containers.put(name, container);
        return container;
    }

    @Override
    public synchronized PlotFrame buildPlotFrame(String stage, String name, Meta meta) {
        if (!containers.containsKey(name)) {
            FXPlotFrame frame = buildFrame(meta);
            frame.configure(meta);
            PlotContainer container = showPlot(name, frame);
            containers.put(name, container);
        }
        return containers.get(name).getPlot();
    }

    @Override
    public PlotFrame getPlotFrame(String stage, String name) throws NameNotFoundException {
        if (!hasPlotFrame(stage, name)) {
            return buildPlotFrame(stage, name, Meta.empty());
        } else {
            PlotContainer container = containers.get(name);
            Window window = container.getRoot().getScene().getWindow();
            if (!window.isShowing()) {
                if (window instanceof Stage) {
                    Platform.runLater(() -> ((Stage) window).show());
                }
            }
            return container.getPlot();
        }
    }

    @Override
    public boolean hasPlotFrame(String stage, String name) {
        return containers.containsKey(name);
    }

}
