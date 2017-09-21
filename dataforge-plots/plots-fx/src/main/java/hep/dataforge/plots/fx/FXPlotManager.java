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
import javafx.scene.Scene;
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
@PluginDef(name = "plots", group = "hep.dataforge", dependsOn = {"hep.dataforge:fx"}, info = "Basic plottiong plugin")
public class FXPlotManager extends BasicPlugin implements PlotManager {
    public static final String FX_FRAME_TYPE_KEY = "fxFrame.type";

    private static ServiceLoader<FXPlotFrameFactory> fxPlotFrameFactoryServiceLoader = ServiceLoader.load(FXPlotFrameFactory.class);

    private final Map<String, Map<String, PlotContainer>> stages = new HashMap<>();

    /**
     * Build an FX frame of the given type using spi.
     *
     * @return
     */
    public static FXPlotFrame buildFXPlotFrame(Meta meta) {
        String type = meta.getString(FX_FRAME_TYPE_KEY, "");
        return StreamSupport.stream(fxPlotFrameFactoryServiceLoader.spliterator(), false)
                .filter(it -> type.isEmpty() || it.getName().equals(type))
                .sorted()
                .findFirst()
                .orElseThrow(() -> new NameNotFoundException(type))
                .build(meta);
    }

    private synchronized Map<String, PlotContainer> getStage(String stage) {
        return stages.computeIfAbsent(stage, stageName -> new HashMap<>());
    }

    protected FXPlotFrame buildFrame(Meta meta) {
        return buildFXPlotFrame(new Laminate(meta, meta()));
    }

    protected synchronized PlotContainer showPlot(String name, FXPlotFrame frame) {
        FXPlugin fx = getContext().getFeature(FXPlugin.class);
        PlotContainer container = FXPlotUtils.displayContainer(fx, name, 800, 600);
        container.setPlot(frame);
        return container;
    }

    public synchronized PlotContainer getPlotContainer(String stage, String name) {
        return getStage(stage).computeIfAbsent(name, plotName -> {
            FXPlotFrame frame = buildFrame(meta());
            return showPlot(name, frame);
        });
    }

    @Override
    public PlotFrame getPlotFrame(String stage, String name) throws NameNotFoundException {
        PlotContainer container = getPlotContainer(stage, name);
        Platform.runLater(() -> {
            Scene scene = container.getPane().getScene();
            if (scene != null) {
                Window window = container.getPane().getScene().getWindow();
                if (window != null && !window.isShowing() && window instanceof Stage) {
                    ((Stage) window).show();
                }
            }
        });
        return container.getPlot();
    }

    @Override
    public boolean hasPlotFrame(String stage, String name) {
        return stages.containsKey(stage) && stages.get(stage).containsKey(name);
    }

}
