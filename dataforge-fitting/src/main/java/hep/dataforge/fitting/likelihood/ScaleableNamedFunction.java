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
package hep.dataforge.fitting.likelihood;

import hep.dataforge.names.AbstractNamedSet;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;
import static java.lang.Math.exp;
import hep.dataforge.fitting.parametric.ParametricValue;

/**
 * <p>Abstract ScaleableNamedFunction class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class ScaleableNamedFunction extends AbstractNamedSet implements ParametricValue{

    /**
     * На сколько в логарифме смещается значение
     */
    private double scale = 0;

    /**
     * <p>Constructor for ScaleableNamedFunction.</p>
     *
     * @param names a {@link hep.dataforge.names.Names} object.
     */
    public ScaleableNamedFunction(Names names) {
        super(names);
    }
    
    /**
     * <p>Constructor for ScaleableNamedFunction.</p>
     *
     * @param named a {@link hep.dataforge.names.NameSetContainer} object.
     */
    public ScaleableNamedFunction(NameSetContainer named) {
        super(named.names());
    }    

    /**
     * <p>expDeriv.</p>
     *
     * @param derivName a {@link java.lang.String} object.
     * @param point a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a double.
     */
    public double expDeriv(String derivName, NamedValueSet point) {
        return expValue(point)*this.derivValue(derivName, point);
    }

    /**
     * <p>expValue.</p>
     *
     * @param point a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a double.
     */
    public double expValue(NamedValueSet point) {
        return exp(this.value(point) + scale);
        
    }

    /**
     * <p>Getter for the field <code>scale</code>.</p>
     *
     * @return a double.
     */
    public double getScale() {
        return scale;
    }

    public void reScale(NamedValueSet pars) {
        this.scale = -this.value(pars);
    }

    /**
     * Параметр scale вводится чтобы экспонентное значение не было нулем
     *
     * @param logShift a double.
     */
    public void setScale(double logShift) {
        this.scale = logShift;
    }
}
