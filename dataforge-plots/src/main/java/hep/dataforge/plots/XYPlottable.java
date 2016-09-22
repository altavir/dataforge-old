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
package hep.dataforge.plots;

import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueUtils;

import java.util.stream.Stream;

import static hep.dataforge.fx.configuration.MetaTreeItem.NO_CONFIGURATOR_TAG;
import static hep.dataforge.meta.Configuration.FINAL_TAG;

/**
 * Подкласс для рисования двумерных картинок
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "color", info = "The color of line or symbol.")
@ValueDef(name = "showLine", type = "BOOLEAN", def = "true", info = "Show the connecting line.")
@ValueDef(name = "showSymbol", type = "BOOLEAN", def = "false", info = "Show symbols for data point.")
//@ValueDef(name = "symbolType", info = "The type of the symbols for scatterplot.")
//@ValueDef(name = "symbolSize", type = "NUMBER", info = "The size of the symbols for scatterplot.")
//@ValueDef(name = "lineType", info = "The type of the line fill.")
@ValueDef(name = "connectionType", allowed = "[default, step, spline]", def = "default", info = "The type of conncetion between points.")
@ValueDef(name = "thickness", type = "NUMBER", info = "The type of the line.")
@NodeDef(name = "adapter", info = "An adapter to interpret the dataset",
        target = "class::hep.dataforge.tables.XYAdapter", tags = {NO_CONFIGURATOR_TAG, FINAL_TAG})
public abstract class XYPlottable extends AbstractPlottable<XYAdapter> implements Plottable<XYAdapter> {

    public XYPlottable(String name) {
        super(name);
    }

    public XYPlottable(String name, XYAdapter adapter) {
        super(name, adapter);
    }

    public Stream<DataPoint> plotData(Value from, Value to) {
        return dataStream(new MetaBuilder("").putValue("xRange.from", from).putValue("xRange.to", to));
    }

    public Stream<DataPoint> plotData(Value from, Value to, int maxPoints) {
        return dataStream(new MetaBuilder("").putValue("xRange.from", from).putValue("xRange.to", to).putValue("maxPoints", maxPoints));
    }


    protected Stream<DataPoint> filterXRange(Stream<DataPoint> data, Meta xRange) {
        Value from = xRange.getValue("from", Value.NULL);
        Value to = xRange.getValue("to", Value.NULL);
        if (from != Value.NULL && to != Value.NULL) {
            return data.filter(point -> ValueUtils.isBetween(adapter().getX(point), from, to));
        } else if (from == Value.NULL && to != Value.NULL) {
            return data.filter(point -> ValueUtils.compare(adapter().getX(point), to) < 0);
        } else if (to == Value.NULL) {
            return data.filter(point -> ValueUtils.compare(adapter().getX(point), from) > 0);
        } else {
            return data;
        }
    }

    protected Stream<DataPoint> filterDataStream(Stream<DataPoint> data, Meta cfg) {
        if (cfg.isEmpty()) {
            return data;
        }
        if (cfg.hasNode("xRange")) {
            data = filterXRange(data, cfg.getNode("xRange"));
        }
        return data;
    }

    @Override
    protected XYAdapter buildAdapter(Meta adapterMeta) {
        return new XYAdapter(adapterMeta);
    }

    @Override
    protected XYAdapter defaultAdapter() {
        return XYAdapter.DEFAULT_ADAPTER;
    }
}
