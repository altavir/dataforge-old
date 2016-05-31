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
package hep.dataforge.likelihood;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.functions.NamedFunction;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.maths.NamedMatrix;
import static java.lang.Math.log;
import static hep.dataforge.names.NamedUtils.areEqual;
import hep.dataforge.values.NamedValueSet;

/**
 * Конструирует апостериорный логарифм правдоподобия по ковариационной матрице,
 * предполагая распределение нормальным.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class AnalyticalPosteriorLogLike extends ScaleableNamedFunction {

    NamedGaussianPDFLog like;
    NamedFunction priorProb = null;
    
    /**
     * <p>Constructor for AnalyticalPosteriorLogLike.</p>
     *
     * @param centralValues a {@link hep.dataforge.maths.NamedVector} object.
     * @param covariance a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public AnalyticalPosteriorLogLike(NamedVector centralValues, NamedMatrix covariance) {
        super(centralValues);
        if (!areEqual(covariance.names(), centralValues.names())) {
            throw new IllegalArgumentException("Different names for centralValues and covariance.");
        }
        this.like = new NamedGaussianPDFLog(centralValues, covariance);
    }    
    
    /**
     * <p>Constructor for AnalyticalPosteriorLogLike.</p>
     *
     * @param centralValues a {@link hep.dataforge.maths.NamedVector} object.
     * @param covariance a {@link hep.dataforge.maths.NamedMatrix} object.
     * @param priorProb a {@link hep.dataforge.functions.NamedFunction} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public AnalyticalPosteriorLogLike(NamedVector centralValues, NamedMatrix covariance, NamedFunction priorProb) throws NameNotFoundException {
        this(centralValues, covariance);
        if (!centralValues.names().contains(priorProb.namesAsArray())) {
            throw new IllegalArgumentException("Wrong names for priorProb.");
        }
        this.priorProb = priorProb;
    }
    
    /** {@inheritDoc} */
    @Override
    public double derivValue(String derivParName, NamedValueSet pars) {
        double res = this.like.derivValue(derivParName, pars);
        if (priorProb != null) {
            res += priorProb.derivValue(derivParName, pars) / priorProb.value(pars);
        }        
        return res;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean providesDeriv(String name) {
        if (this.priorProb == null) {
            return true;
        } else {
            return this.priorProb.providesDeriv(name);
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public double value(NamedValueSet pars) {
        double res = this.like.value(pars);
        if (priorProb != null) {
            res += log(priorProb.value(pars));
        }
        return res;
    }
}
