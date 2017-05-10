package hep.dataforge.plots.gnuplot;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.Plot;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.XYPlotFrame;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by darksnake on 10-May-17.
 */
public class GnuPlotFrame extends XYPlotFrame {

    private JavaPlot javaPlot = new JavaPlot();
    private Map<String, Plot> plots = new HashMap<>();
    private GNUPlotTerminal terminal;

    @Override
    protected void updatePlotData(String name) {
        opt(name).ifPresent(plottable -> {
            plots.computeIfAbsent(name, str -> new DataSetPlot(new PlottableDataSet(plottable)));
        });

    }

    @Override
    protected void updatePlotConfig(String name) {

    }

    @Override
    protected void updateFrame(Meta annotation) {

    }

    @Override
    protected void updateAxis(String axisName, Meta axisMeta, Meta plotMeta) {

    }

    @Override
    protected void updateLegend(Meta legendMeta) {

    }

    /**
     * Update result
     */
    public void refresh() {

    }

    public String getCommands() {
        return javaPlot.getCommands();
    }
}
