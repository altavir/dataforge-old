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

import hep.dataforge.fitting.Param;
import hep.dataforge.fitting.ParamSet;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;
import hep.dataforge.fitting.parametric.ParametricValue;

/**
 * <p>MultivariateGaussianPrior class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class MultivariateGaussianPrior implements ParametricValue {

    private final ParamSet set;

    /**
     * <p>Constructor for MultivariateGaussianPrior.</p>
     *
     * @param set a {@link hep.dataforge.fitting.ParamSet} object.
     */
    public MultivariateGaussianPrior(ParamSet set) {
        this.set = set;
    }

    /** {@inheritDoc} */
    @Override
    public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException, NameNotFoundException {
        if (set.names().contains(derivParName)) {
            double mean = set.getDouble(derivParName);
            double sigma = set.getError(derivParName);
            double value = pars.getDouble(derivParName);
            double dif = value - mean;

            return -this.value(pars) * dif / sigma / sigma;
        } else {
            return 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Names names() {
        return set.names();
    }

    /** {@inheritDoc} */
    @Override
    public boolean providesDeriv(String name) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public double value(NamedValueSet pars) throws NameNotFoundException {
        double res = 1;
        for (Param par : set.getParams()) {
            
            double mean = par.value();
            double sigma = par.getErr();
            double value = pars.getDouble(par.name());
            double dif = value - mean;
            
            res *= 1 / Math.sqrt(2 * Math.PI) / sigma * Math.exp(-dif * dif / 2 / sigma / sigma);
        }
        return res;
    }

}
