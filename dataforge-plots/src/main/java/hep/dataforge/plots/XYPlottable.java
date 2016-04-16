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
import hep.dataforge.points.DataPoint;
import hep.dataforge.points.XYAdapter;
import hep.dataforge.values.Value;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Подкласс для рисования двумерных картинок
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "color", info = "The color of line or symbol.")
@ValueDef(name = "showLine", type = "BOOLEAN", def = "true", info = "Show the connecting line.")
@ValueDef(name = "showSymbol", type = "BOOLEAN", def = "false", info = "Show symbols for data point.")
@ValueDef(name = "symbolType", info = "The type of the symbols for scatterplot.")
@ValueDef(name = "symbolSize", type = "NUMBER", info = "The size of the symbols for scatterplot.")
@ValueDef(name = "lineType", info = "The type of the line fill.")
@ValueDef(name = "connectionType", allowed = "[default, step, spline]", info = "The type of conncetion between points.")
@ValueDef(name = "thickness", type = "NUMBER", info = "The type of the line.")
@NodeDef(name = "adapter", info = "An adapter to interpret the dataset", target = "class::hep.dataforge.points.XYAdapter")
public abstract class XYPlottable extends AbstractPlottable implements Plottable {
    
    public XYPlottable(String name) {
        super(name);
    }    

    public Collection<DataPoint> plotData(Value from, Value to) {
        return plotData().stream().filter((dp) -> adapter().getX(dp).isBetween(from, to)).collect(Collectors.toList());
    }

    /**
     * An adapter to interpret points. Could be overridden by custom adapter
     *
     * @return
     */
    public XYAdapter adapter() {
        if (meta().hasNode("adapter")) {
            return new XYAdapter(meta().getNode("adapter"));
        } else {
            return new XYAdapter();
        }
    }
}
