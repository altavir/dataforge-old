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
package hep.dataforge.stat.likelihood;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.names.Names;
import hep.dataforge.stat.parametric.ParametricValue;
import hep.dataforge.values.NamedValueSet;

/**
 * <p>GaussianPrior class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class GaussianPrior implements ParametricValue{
    private final String parName;
//    UnivariateDifferentiableFunction gaussian;
    double mean;
    double sigma;
    private Names names;

    /**
     * <p>Constructor for GaussianPrior.</p>
     *
     * @param parName a {@link java.lang.String} object.
     * @param mean a double.
     * @param sigma a double.
     */
    public GaussianPrior(String parName, double mean, double sigma) {
        this.parName = parName;
        this.mean = mean;
        this.sigma = sigma;
        names = Names.of(parName);
//        this.gaussian = new Gaussian(mean, sigma);
    }

    /** {@inheritDoc} */
    @Override
    public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException, NameNotFoundException {
        double value = pars.getDouble(parName);
        double dif = value-mean;

        return -this.value(pars) * dif / sigma / sigma;
//        if(this.parName.equalsIgnoreCase(derivParName)){
//            DerivativeStructure struct = new DerivativeStructure(1, 1, 1, pars.getDouble(parName));
//            double val = gaussian.value(struct).getPartialDerivative(1);
//            return val;
//        } else {
//            return 0;
//        }
        
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public Names names() {
        return this.names;
    }

    /** {@inheritDoc} */
    @Override
    public boolean providesDeriv(String name) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public double value(NamedValueSet pars) throws NameNotFoundException {
//        return this.gaussian.value(pars.getDouble(parName));
        double value = pars.getDouble(parName);
        double dif = value-mean;
        
        return 1/Math.sqrt(2*Math.PI)/sigma*Math.exp(-dif*dif/2/sigma/sigma);
    }

    
}
