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
package hep.dataforge.datafitter.models;

import hep.dataforge.data.DataPoint;
import hep.dataforge.data.XYDataAdapter;
import hep.dataforge.functions.ParametricFunction;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.meta.Meta;
import java.util.function.Function;

/**
 * The XYModel in which errors in some (or all) point are increased in comparison with errors provided by DataSet
 * The weightFunction describes the increase in dispersion (not errors!) for each point.
 *
 * @author darksnake
 * @version $Id: $Id
 */
public class WeightedXYModel extends XYModel {
    
    private final Function<DataPoint, Double> weightFunction;

    /**
     * <p>Constructor for WeightedXYModel.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param source a {@link hep.dataforge.functions.ParametricFunction} object.
     * @param weightFunction a {@link java.util.function.Function} object.
     */
    public WeightedXYModel(String name, ParametricFunction source, Function<DataPoint, Double> weightFunction) {
        super(name, source);
        this.weightFunction = weightFunction;
    }

    /**
     * <p>Constructor for WeightedXYModel.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param source a {@link hep.dataforge.functions.ParametricFunction} object.
     * @param format a {@link hep.dataforge.data.XYDataAdapter} object.
     * @param weightFunction a {@link java.util.function.Function} object.
     */
    public WeightedXYModel(String name, ParametricFunction source, XYDataAdapter format, Function<DataPoint, Double> weightFunction) {
        super(name, source, format);
        this.weightFunction = weightFunction;
    }

    /**
     * <p>Constructor for WeightedXYModel.</p>
     *
     * @param source a {@link hep.dataforge.functions.ParametricFunction} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param weightFunction a {@link java.util.function.Function} object.
     */
    public WeightedXYModel(ParametricFunction source, Meta annotation, Function<DataPoint, Double> weightFunction) {
        super(source, annotation);
        this.weightFunction = weightFunction;
    }

    /** {@inheritDoc} */
    @Override
    public double dispersion(DataPoint point, NamedDoubleSet pars) {
        return super.dispersion(point, pars)*weightFunction.apply(point);
    }
    
    
    
}
