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

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.MapPoint;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.utils.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Nozik
 */
@ValueDef(name = "showLine", type = "BOOLEAN", def = "false", info = "Show the connecting line.")
@ValueDef(name = "showSymbol", type = "BOOLEAN", def = "true", info = "Show symbols for data point.")
@ValueDef(name = "showErrors", def = "true", info = "Show errors for points.")
public class PlottableData extends XYPlottable {

    public static PlottableData plot(String name, double[] x, double[] y, double[] xErrs, double[] yErrs) {
        PlottableData plot = new PlottableData(name);

        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays size mismatch");
        }

        List<DataPoint> data = new ArrayList<>();
        for (int i = 0; i < y.length; i++) {
            MapPoint.Builder point = new MapPoint(new String[]{XYAdapter.X_VALUE_KEY, XYAdapter.Y_VALUE_KEY}, x[i], y[i]).builder();

            if (xErrs != null) {
                point.putValue(XYAdapter.X_ERROR_KEY, xErrs[i]);
            }

            if (yErrs != null) {
                point.putValue(XYAdapter.Y_ERROR_KEY, yErrs[i]);
            }

            data.add(point.build());
        }
        plot.setData(data);
        return plot;
    }

    public static PlottableData plot(String name, double[] x, double[] y) {
        return plot(name, x, y, null, null);
    }

    public static PlottableData plot(String name, XYAdapter adapter, boolean showErrors) {
        MetaBuilder builder = new MetaBuilder("dataPlot").setValue("showErrors", showErrors);
        PlottableData plot = new PlottableData(name);
        plot.setAdapter(adapter);
        plot.configure(builder);
        return plot;
    }

    public static PlottableData plot(String name, XYAdapter adapter, Iterable<DataPoint> data) {
        PlottableData plot = plot(name, adapter, true);
        plot.fillData(data);
        return plot;
    }

    public static PlottableData plot(String name, Meta meta, XYAdapter adapter, Iterable<DataPoint> data) {
        PlottableData plot = plot(name, adapter, true);
        plot.fillData(data);
        if (!meta.isEmpty()) {
            plot.configure(meta);
        }
        return plot;
    }


    //TODO replace by ObservableList and allow external modification
    protected List<DataPoint> data = new ArrayList<>();

    public PlottableData(String name) {
        super(name);
    }

    /**
     * Non safe method to set data to this plottable. The list must be immutable
     *
     * @param data
     */
    public void setData(@NonNull List<DataPoint> data) {
        this.data = data;
    }

    public void fillData(Iterable<DataPoint> it, boolean append) {
        if (this.data == null || !append) {
            this.data = new ArrayList<>();
        }
        for (DataPoint dp : it) {
            data.add(dp);
        }
        notifyDataChanged();
    }

    /**
     * Safe mtethod to add data
     *
     * @param it
     */
    public void fillData(@NonNull Iterable<DataPoint> it) {
        fillData(it, false);
    }

    @Override
    protected List<DataPoint> getRawData(Meta query) {
        return data;
    }

}
