package hep.dataforge.plots.gnuplot;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.Plot;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by darksnake on 10-May-17.
 */
public class GnuPlotFrame extends XYPlotFrame {

    private JavaPlot javaPlot = new JavaPlot();
    private Map<String, DataSetPlot> plots = new HashMap<>();
    private GNUPlotTerminal terminal;

    @Override
    protected void updatePlotData(String name, @NotNull Plot plot) {
        plots.computeIfAbsent(name, str -> {
            DataSetPlot dpl = new DataSetPlot(new PlottableDataSet(plot));
            javaPlot.addPlot(dpl);
            dpl.setTitle(plot.meta().getString("title", plot.getName()));
            return dpl;
        });

        refresh();
    }

    @Override
    protected void updatePlotConfig(String name, Laminate laminate) {

    }

    @Override
    protected void updateFrame(Meta meta) {
        meta.optValue("gnuplot.path").map(Value::stringValue).ifPresent(path -> {
            try {
                javaPlot.setGNUPlotPath(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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

    /**
     * Push the plot to the output
     */
    public void plot() {
        javaPlot.plot();
    }

    public String getCommands() {
        return javaPlot.getCommands();
    }
}
