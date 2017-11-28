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

import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.tables.Adapters;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueUtils;
import hep.dataforge.values.Values;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hep.dataforge.meta.Configuration.FINAL_TAG;
import static hep.dataforge.tables.ValuesAdapter.ADAPTER_KEY;
import static hep.dataforge.values.ValueType.BOOLEAN;
import static hep.dataforge.values.ValueType.NUMBER;

/**
 * Plot with x and y axis. It is possible to have multiple y axis
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "color", info = "The color of line or symbol.", tags = {"color"})
@ValueDef(name = "showLine", type = {BOOLEAN}, def = "true", info = "Show the connecting line.")
@ValueDef(name = "showSymbol", type = {BOOLEAN}, def = "true", info = "Show symbols for data point.")
//@ValueDef(name = "symbolType", info = "The type of the symbols for scatterplot.")
//@ValueDef(name = "symbolSize", type = "NUMBER", info = "The size of the symbols for scatterplot.")
//@ValueDef(name = "lineType", info = "The type of the line fill.")
@ValueDef(name = "connectionType", allowed = "[default, step, spline]", def = "default", info = "The type of conncetion between points.")
@ValueDef(name = "thickness", type = {NUMBER}, info = "The type of the line.")
@NodeDef(name = ADAPTER_KEY, info = "An adapter to interpret the dataset",
        from = "class::hep.dataforge.tables.XYAdapter", tags = {FINAL_TAG})
public abstract class XYPlot extends AbstractPlot {

    public XYPlot(String name) {
        super(name);
    }

    public List<Values> getData(Value from, Value to) {
        return getData(new MetaBuilder("").putValue("xRange.from", from).putValue("xRange.to", to));
    }

    public List<Values> getData(Value from, Value to, int numPoints) {
        return getData(new MetaBuilder("").putValue("xRange.from", from).putValue("xRange.to", to).putValue("numPoints", numPoints));
    }

    /**
     * Apply range filters to data
     *
     * @param query
     * @return
     */
    @NodeDef(name = "xRange", info = "X filter")
    @ValueDef(name = "xRange.from", type = {NUMBER}, info = "X range from")
    @ValueDef(name = "xRange.to", type = {NUMBER}, info = "X range to")
    @ValueDef(name = "numPoints", type = {NUMBER}, info = "A required number of visible points. The real number could differ from requested one.")
    @Override
    public List<Values> getData(Meta query) {
        if (query.isEmpty()) {
            return getRawData(query);
        } else {
            return filterDataStream(getRawData(query).stream(), query).collect(Collectors.toList());
        }
    }

    protected abstract List<Values> getRawData(Meta query);

    protected Stream<Values> filterXRange(Stream<Values> data, Meta xRange) {
        Value from = xRange.getValue("from", Value.NULL);
        Value to = xRange.getValue("to", Value.NULL);
        if (from != Value.NULL && to != Value.NULL) {
            return data.filter(point -> ValueUtils.isBetween(Adapters.getXValue(getAdapter(),point), from, to));
        } else if (from == Value.NULL && to != Value.NULL) {
            return data.filter(point -> ValueUtils.compare(Adapters.getXValue(getAdapter(),point), to) < 0);
        } else if (to == Value.NULL) {
            return data.filter(point -> ValueUtils.compare(Adapters.getXValue(getAdapter(),point), from) > 0);
        } else {
            return data;
        }
    }

    protected Stream<Values> filterDataStream(Stream<Values> data, Meta cfg) {
        if (cfg.isEmpty()) {
            return data;
        }
        if (cfg.hasMeta("xRange")) {
            data = filterXRange(data, cfg.getMeta("xRange"));
        }
        return data;
    }

}
