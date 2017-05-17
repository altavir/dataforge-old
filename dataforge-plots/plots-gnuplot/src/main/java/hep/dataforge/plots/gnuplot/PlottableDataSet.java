package hep.dataforge.plots.gnuplot;

import com.panayotis.gnuplot.dataset.DataSet;
import hep.dataforge.plots.Plottable;

/**
 * Simple wrapper for a plottable
 * Created by darksnake on 10-May-17.
 */
public class PlottableDataSet implements DataSet {
    private final Plottable plottable;
    private String[] mapping;

    public PlottableDataSet(Plottable plottable) {
        this.plottable = plottable;
        mapping = new String[]{"x.value", "y.value"};
    }

    @Override
    public int size() {
        return plottable.getData().size();
    }

    @Override
    public int getDimensions() {
        return mapping.length;
    }

    @Override
    public String getPointValue(int point, int dimension) {
        return plottable.getComponent(point, mapping[dimension]).stringValue();
    }
}
