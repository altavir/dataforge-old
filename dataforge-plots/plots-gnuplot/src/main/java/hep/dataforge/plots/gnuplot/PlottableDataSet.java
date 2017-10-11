package hep.dataforge.plots.gnuplot;

import com.panayotis.gnuplot.dataset.DataSet;
import hep.dataforge.plots.Plot;

/**
 * Simple wrapper for a plot
 * Created by darksnake on 10-May-17.
 */
public class PlottableDataSet implements DataSet {
    private final Plot plot;
    private String[] mapping;

    public PlottableDataSet(Plot plot) {
        this.plot = plot;
        mapping = new String[]{"x.value", "y.value"};
    }

    @Override
    public int size() {
        return plot.getData().size();
    }

    @Override
    public int getDimensions() {
        return mapping.length;
    }

    @Override
    public String getPointValue(int point, int dimension) {
        return plot.getComponent(point, mapping[dimension]).stringValue();
    }
}
