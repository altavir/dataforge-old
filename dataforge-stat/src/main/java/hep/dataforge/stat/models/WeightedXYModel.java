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
package hep.dataforge.stat.models;

import hep.dataforge.meta.Meta;
import hep.dataforge.stat.parametric.ParametricFunction;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.Values;

import java.util.function.Function;

/**
 * The XYModel in which errors in some (or all) point are increased in comparison with errors provided by DataSet
 * The weightFunction describes the increase in dispersion (not errors!) for each point.
 *
 * @author darksnake
 * @version $Id: $Id
 */
public class WeightedXYModel extends XYModel {
    
    private final Function<Values, Double> weightFunction;

    public WeightedXYModel(ParametricFunction source, Function<Values, Double> weightFunction) {
        super(source);
        this.weightFunction = weightFunction;
    }

    public WeightedXYModel(ParametricFunction source, XYAdapter format, Function<Values, Double> weightFunction) {
        super(source, format);
        this.weightFunction = weightFunction;
    }

    /**
     * <p>Constructor for WeightedXYModel.</p>
     *
     * @param source a {@link hep.dataforge.stat.parametric.ParametricFunction} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param weightFunction a {@link java.util.function.Function} object.
     */
    public WeightedXYModel(ParametricFunction source, Meta annotation, Function<Values, Double> weightFunction) {
        super(source, annotation);
        this.weightFunction = weightFunction;
    }

    /** {@inheritDoc} */
    @Override
    public double dispersion(Values point, Values pars) {
        return super.dispersion(point, pars)*weightFunction.apply(point);
    }

    @Override
    public void setMeta(Meta meta) {
        super.setMeta(meta);
    }
}
