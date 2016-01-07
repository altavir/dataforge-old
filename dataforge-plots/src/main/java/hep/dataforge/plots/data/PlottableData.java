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
package hep.dataforge.plots.data;

import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.data.XYDataAdapter;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.XYPlottable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "showLine", type = "BOOLEAN", def = "false", info = "Show the connecting line.")
@ValueDef(name = "showSymbol", type = "BOOLEAN", def = "true", info = "Show symbols for data point.")
@ValueDef(name = "showErrors", def = "true", info = "Show errors for points.")
public class PlottableData extends XYPlottable {

    public static PlottableData plot(String name, double[] x, double[] y, double[] xErrs, double[] yErrs) {
        PlottableData plot = new PlottableData(name, null);

        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays size mismatch");
        }

        List<DataPoint> data = new ArrayList<>();
        for (int i = 0; i < y.length; i++) {
            MapDataPoint point = new MapDataPoint(new String[]{"x", "y"}, x[i], y[i]);

            if (xErrs != null) {
                point.putValue("xErr", xErrs[i]);
            }

            if (yErrs != null) {
                point.putValue("yErr", yErrs[i]);
            }

            data.add(point);
        }
        plot.fillData(data);
        return plot;
    }

    public static PlottableData plot(String name, double[] x, double[] y) {
        return plot(name, x, y, null, null);
    }

    public static PlottableData plot(String name, XYDataAdapter adapter, boolean showErrors) {
        MetaBuilder builder = new MetaBuilder("dataPlot")
                .setNode("adapter", adapter.meta());
        builder.setValue("showErrors", showErrors);
        PlottableData plot = new PlottableData(name, builder.build());
        return plot;
    }

    public static PlottableData plot(String name, XYDataAdapter adapter, Iterable<DataPoint> data) {
        PlottableData plot = plot(name, adapter, true);
        plot.fillData(data);
        return plot;
    }
    
    public static PlottableData plot(DataSet data, XYDataAdapter adapter){
        PlottableData plot = plot(data.getName(), adapter, true);
        plot.fillData(data);
        if(data.meta().hasNode("plot")){
            plot.configure(data.meta().getNode("plot"));
        }
        return plot;
    }

    protected List<DataPoint> data;

    protected PlottableData(String name, Meta meta) {
        super(name, meta);
        data = new ArrayList<>();
    }

    public void fillData(Iterable<DataPoint> it) {
        this.data = new ArrayList<>();
        for (DataPoint dp : it) {
            data.add(dp);
        }
        notifyDataChanged();
    }

//    private static Meta extractMeta(DataSet data) {
//        return data.meta().getNode("plot", Meta.buildEmpty("plot"));
//    }
    @Override
    public Collection<DataPoint> plotData() {
        return data;
    }

}
