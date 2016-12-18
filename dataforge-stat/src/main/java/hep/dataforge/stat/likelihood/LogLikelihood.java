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

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.stat.fit.FitState;
import hep.dataforge.stat.fit.ParamSet;
import hep.dataforge.stat.parametric.AbstractParametricValue;
import hep.dataforge.stat.parametric.FunctionUtils;
import hep.dataforge.stat.parametric.ParametricValue;
import hep.dataforge.values.NamedValueSet;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * LogLikelihood class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class LogLikelihood extends ScaleableNamedFunction {

    private final FitState source;

    public LogLikelihood(FitState source) {
        super(source.getModel());
        this.source = source;
        if (!source.getModel().providesProb()) {
            LoggerFactory.getLogger(getClass())
                    .info("LogLikelihood : Model does not provide definition for point destribution. Using -chi^2/2 for logLikelihood.");
        }
    }

    public double derivValue(String derivParName, ParamSet pars) {
        return source.getLogProbDeriv(derivParName, pars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException, NamingException {
        return derivValue(derivParName, new ParamSet(pars));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return source.getModel().size();
    }

    /**
     * <p>
     * getLikelihood.</p>
     *
     * @return a {@link hep.dataforge.stat.parametric.ParametricValue} object.
     */
    public ParametricValue getLikelihood() {
        return new AbstractParametricValue(this) {
            @Override
            public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException, NamingException {
                return expDeriv(derivParName, pars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return LogLikelihood.this.providesDeriv(name);
            }

            @Override
            public double apply(NamedValueSet pars) throws NamingException {
                return expValue(pars);
            }
        };
    }

    public UnivariateFunction getLogLikelihoodProjection(final String axisName, final NamedValueSet allPar) {
        return FunctionUtils.getNamedProjection(this, axisName, allPar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesDeriv(String name) {
        return source.getModel().providesProbDeriv(name);
    }

    public double value(ParamSet pars) {
        return source.getLogProb(pars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double apply(NamedValueSet pars) throws NamingException {
        return value(new ParamSet(pars));
    }

}
