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
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.XYPlottable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "showErrors", def = "true", info = "Show errors for points.")
public class PlottableData extends XYPlottable {

    protected List<DataPoint> data;

    protected PlottableData(String name, Meta meta){
        super(name, meta);
        data = new ArrayList<>();
    }
    
    public PlottableData(String name, double[] x, double[] y) {
        this(name, x, y, null, null);
    }

    public PlottableData(String name, double[] x, double[] y, double[] xErrs, double[] yErrs) {
        super(name, null);

        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays size mismatch");
        }

        this.data = new ArrayList<>();
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
    }

    public PlottableData(String name, Meta annotation, Iterable<DataPoint> data) {
        super(name, annotation);
        fillData(data);
    }

    public PlottableData(DataSet data, String xName, String yName) {
        this(data.getName(), extractMeta(data), data, xName, yName);
    }

    public PlottableData(DataSet data, String xName, String yName, String xErrName, String yErrName) {
        this(data.getName(), extractMeta(data), data, xName, yName, xErrName, yErrName);
    }

    public PlottableData(String name, Meta meta, Iterable<DataPoint> data, String xName, String yName) {
        this(name, meta, data);
        getConfig().setValue("adapter.x", xName);
        getConfig().setValue("adapter.y", yName);
        getConfig().setValue("showErrors", false);
    }

    public PlottableData(String name, Meta meta, Iterable<DataPoint> data, String xName, String yName, String xErrName, String yErrName) {
        this(name, meta, data);
        getConfig().setValue("adapter.x", xName);
        getConfig().setValue("adapter.y", yName);
        getConfig().setValue("showErrors", false);

        if (xErrName != null) {
            getConfig().setValue("adapter.xErr", xErrName);
        }

        if (yErrName != null) {
            getConfig().setValue("adapter.yErr", yErrName);
        }
    }

    protected void fillData(Iterable<DataPoint> it) {
        this.data = new ArrayList<>();
        for (DataPoint dp : it) {
            data.add(dp);
        }
    }
    
    private static Meta extractMeta(DataSet data){
        return data.meta().getNode("plot", Meta.buildEmpty("plot"));
    }

    @Override
    public Collection<DataPoint> plotData() {
        return data;
    }

}
