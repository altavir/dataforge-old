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
import hep.dataforge.tables.Adapters;
import hep.dataforge.tables.ValueMap;
import hep.dataforge.tables.ValuesAdapter;
import hep.dataforge.values.Values;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static hep.dataforge.tables.Adapters.*;
import static hep.dataforge.values.ValueType.BOOLEAN;

/**
 * @author Alexander Nozik
 */
@ValueDef(name = "showLine", type = {BOOLEAN}, def = "false", info = "Show the connecting line.")
@ValueDef(name = "showSymbol", type = {BOOLEAN}, def = "true", info = "Show symbols for data point.")
@ValueDef(name = "showErrors", type = {BOOLEAN}, def = "true", info = "Show errors for points.")
public class DataPlot extends XYPlot {
    public static DataPlot plot(String name, double[] x, double[] y, double[] xErrs, double[] yErrs) {
        DataPlot plot = new DataPlot(name);

        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays size mismatch");
        }

        List<Values> data = new ArrayList<>();
        for (int i = 0; i < y.length; i++) {
            ValueMap.Builder point = ValueMap.of(new String[]{X_AXIS, Y_AXIS}, x[i], y[i]).builder();

            if (xErrs != null) {
                point.putValue(X_ERROR_KEY, xErrs[i]);
            }

            if (yErrs != null) {
                point.putValue(Y_ERROR_KEY, yErrs[i]);
            }

            data.add(point.build());
        }
        plot.setData(data);
        return plot;
    }

    public static DataPlot plot(String name, double[] x, double[] y) {
        return plot(name, x, y, null, null);
    }

    public static DataPlot plot(String name, ValuesAdapter adapter, boolean showErrors) {
        MetaBuilder builder = new MetaBuilder("dataPlot").setValue("showErrors", showErrors);
        DataPlot plot = new DataPlot(name);
        plot.setAdapter(adapter);
        plot.configure(builder);
        return plot;
    }

    public static DataPlot plot(String name, ValuesAdapter adapter, Iterable<Values> data) {
        DataPlot plot = plot(name, adapter, true);
        plot.fillData(data);
        return plot;
    }

    protected List<Values> data = new ArrayList<>();

    public DataPlot(String name) {
        super(name);
    }

    public DataPlot(String name, Meta meta) {
        super(name);
        configure(meta);
    }

    /**
     * Non safe method to set data to this plottable. The list must be immutable
     *
     * @param data
     */
    public void setData(@NotNull List<Values> data) {
        this.data = data;
    }

    public DataPlot fillData(Iterable<? extends Values> it, boolean append) {
        if (this.data == null || !append) {
            this.data = new ArrayList<>();
        }
        for (Values dp : it) {
            data.add(dp);
        }
        notifyDataChanged();
        return this;
    }


    /**
     * Safe mtethod to add data
     *
     * @param it
     */
    public DataPlot fillData(@NotNull Iterable<? extends Values> it) {
        return fillData(it, false);
    }

    public DataPlot fillData(@NotNull Stream<? extends Values> it) {
        this.data = new ArrayList<>();
        it.forEach(dp -> data.add(dp));
        notifyDataChanged();
        return this;
    }

    public DataPlot append(Values dp) {
        data.add(dp);
        notifyDataChanged();
        return this;
    }

    public DataPlot append(Number x, Number y) {
        return append(Adapters.buildXYDataPoint(getAdapter(), x.doubleValue(), y.doubleValue()));
    }

    @Override
    protected List<Values> getRawData(Meta query) {
        return data;
    }

    public void clear() {
        data.clear();
        notifyDataChanged();
    }

}
