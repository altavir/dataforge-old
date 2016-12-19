/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.fx.FXPlugin;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.fx.FXLineChartFrame;
import hep.dataforge.plots.fx.FXPlotFrame;
import hep.dataforge.plots.fx.FXPlotUtils;
import hep.dataforge.plots.fx.PlotContainer;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Alexander Nozik
 */
public class DefaultPlotHolder implements PlotHolder, Encapsulated {
    private final Map<String, PlotContainer> containers = new HashMap<>();
    private final Context context;
    private Supplier<FXPlotFrame<?>> plotFrameFactory = () -> new FXLineChartFrame();

    public DefaultPlotHolder(Context context) {
        this.context = context;
    }

    public DefaultPlotHolder(Context context, Supplier<FXPlotFrame<?>> plotFrameFactory) {
        this(context);
        this.plotFrameFactory = plotFrameFactory;
    }

    protected FXPlotFrame<?> buildFrame() {
        return plotFrameFactory.get();
    }

    protected synchronized PlotContainer showPlot(String name, FXPlotFrame frame) {
        FXPlugin fx = context.getPlugin(FXPlugin.class);
        PlotContainer container = FXPlotUtils.displayContainer(fx, name, 800, 600);
        container.setPlot(frame);
        containers.put(name, container);
        return container;
    }

    @Override
    public synchronized PlotFrame buildPlotFrame(String stage, String name, Meta annotation) {
        if (!containers.containsKey(name)) {
            FXPlotFrame<?> frame = buildFrame();
            frame.configure(annotation);
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

    @Override
    public Context getContext() {
        return context;
    }
}
