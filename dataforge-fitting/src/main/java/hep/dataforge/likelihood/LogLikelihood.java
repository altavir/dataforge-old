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

import hep.dataforge.context.GlobalContext;
import hep.dataforge.datafitter.FitSource;
import hep.dataforge.datafitter.ParamSet;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.functions.AbstractNamedFunction;
import hep.dataforge.functions.FunctionUtils;
import hep.dataforge.functions.NamedFunction;
import hep.dataforge.maths.NamedDoubleSet;
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

    private final FitSource source;

    /**
     * <p>
     * Constructor for LogLikelihood.</p>
     *
     * @param source a {@link hep.dataforge.datafitter.FitSource} object.
     */
    public LogLikelihood(FitSource source) {
        super(source.getModel());
        this.source = source;
        if (!source.getModel().providesProb()) {
            LoggerFactory.getLogger(getClass())
                    .info("LogLikelihood : Model does not provide definition for point destribution. Using -chi^2/2 for logLikelihood.");
        }
    }

//    @Override
//    public boolean contains(String... names) {
//        return source.getModel().names().contains(names);
//    }
    /**
     * <p>
     * derivValue.</p>
     *
     * @param derivParName a {@link java.lang.String} object.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a double.
     */
    public double derivValue(String derivParName, ParamSet pars) {
        return source.getLogProbDeriv(derivParName, pars);
    }

    /** {@inheritDoc} */
    @Override
    public double derivValue(String derivParName, NamedDoubleSet pars) throws NotDefinedException, NamingException {
        return derivValue(derivParName, new ParamSet(pars));
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return source.getModel().getDimension();
    }

    /**
     * <p>
     * getLikelihood.</p>
     *
     * @return a {@link hep.dataforge.functions.NamedFunction} object.
     */
    public NamedFunction getLikelihood() {
        return new AbstractNamedFunction(this) {
            @Override
            public double derivValue(String derivParName, NamedDoubleSet pars) throws NotDefinedException, NamingException {
                return expDeriv(derivParName, pars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return LogLikelihood.this.providesDeriv(name);
            }

            @Override
            public double value(NamedDoubleSet pars) throws NamingException {
                return expValue(pars);
            }
        };
    }

    /**
     * <p>
     * getLogLikelihoodProjection.</p>
     *
     * @param axisName a {@link java.lang.String} object.
     * @param allPar a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction}
     * object.
     */
    public UnivariateFunction getLogLikelihoodProjection(final String axisName, final NamedDoubleSet allPar) {
        return FunctionUtils.getNamedProjection(this, axisName, allPar);
    }

    /** {@inheritDoc} */
    @Override
    public boolean providesDeriv(String name) {
        return source.getModel().providesProbDeriv(name);
    }

    /**
     * {@inheritDoc}
     */
    /**
     * <p>
     * value.</p>
     *
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a double.
     */
    public double value(ParamSet pars) {
        return source.getLogProb(pars);
    }

    /** {@inheritDoc} */
    @Override
    public double value(NamedDoubleSet pars) throws NamingException {
        return value(new ParamSet(pars));
    }

}
