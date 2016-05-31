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

import hep.dataforge.datafitter.FitState;
import hep.dataforge.datafitter.Param;
import hep.dataforge.functions.UnivariateSplineWrapper;
import hep.dataforge.io.reports.Reportable;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import hep.dataforge.maths.NamedMatrix;
import static hep.dataforge.names.NamedUtils.exclude;
import java.io.PrintWriter;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import org.apache.commons.math3.analysis.UnivariateFunction;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static hep.dataforge.maths.GridCalculator.getUniformUnivariateGrid;
import static hep.dataforge.names.NamedUtils.exclude;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

/**
 * TODO переделать freePars в varArgs
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class BayesianManager{

    /**
     * Constant <code>DEFAULT_MAX_CALLS=10000</code>
     */
    public static final int DEFAULT_MAX_CALLS = 10000;

    private ConfidenceLimitCalculator previousCalc;
    private String previousPar;
    private FitState previousResult;
    private final Reportable log;

    /**
     * <p>
     * Constructor for BayesianManager.</p>
     * @param log
     */
    public BayesianManager(Reportable log) {
        this.log = log;
    }

    /**
     * A marginalized likelihood function without caching. Recalculates on call.
     *
     * @param parname
     * @param state
     * @param freePars
     * @param numCalls
     * @return
     */
    private UnivariateFunction calculateLikelihood(String parname, FitState state, String[] freePars, int numCalls) {
        if (state == null) {
            throw new IllegalStateException("Fit information is not propertly initialized.");
        }
        LogLikelihood loglike = state.getLogLike();
        String[] parNames = exclude(freePars, parname);

        NamedMatrix matrix = state.getCovariance();
        Marginalizer marginal = new Marginalizer(matrix, loglike, state.getParameters());
        return marginal.getUnivariateMarginalFunction(numCalls, parname, parNames);
    }

    private ConfidenceLimitCalculator getCalculator(String parname, FitState result, String[] freePars) {
        return getCalculator(parname, result, freePars, DEFAULT_MAX_CALLS);
    }

    private ConfidenceLimitCalculator getCalculator(String parname, FitState state, String[] freePars, int numCalls) {
        log.report(format(
                "Calculating marginal likelihood cache for parameter \'%s\'.", parname));
        if ((previousCalc != null) && parname.equals(previousPar) && state.equals(previousResult)) {
            log.report("Using previously stored marginal likelihood cache.");
            return previousCalc;
        } else {
            UnivariateFunction function = this.calculateLikelihood(parname, state, freePars, numCalls);
            Param par = state.getParameters().getByName(parname);
            Double a = max(par.value() - 4 * par.getErr(), par.getLowerBound());
            Double b = min(par.value() + 4 * par.getErr(), par.getUpperBound());
            ConfidenceLimitCalculator calculator = new ConfidenceLimitCalculator(function, a, b);
            // На случай, если нам нужно сделать несколько действий и не пересчитывать все
            previousCalc = calculator;
            previousPar = parname;
            previousResult = state;
            log.report("Likelihood cache calculation completed.");
            return calculator;
        }
    }

    /**
     * <p>
     * getConfidenceInterval.</p>
     *
     * @param parname a {@link java.lang.String} object.
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param freePars an array of {@link java.lang.String} objects.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public FitState getConfidenceInterval(String parname, FitState state, String[] freePars) {
        log.report(
                format("Starting combined confidence limits calculation for parameter \'%s\'.", parname));

        ConfidenceLimitCalculator calculator = this.getCalculator(parname, state, freePars);
        BayesianConfidenceLimit limit = calculator.getLimits();
        limit.parName = parname;
        limit.freePars = freePars;
        log.report("Confidence limit calculation completed.");
        return state.edit().setInterval(limit).build();
    }

    /**
     * <p>
     * getMarginalLikelihood.</p>
     *
     * @param parname a {@link java.lang.String} object.
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param freePars a {@link java.lang.String} object.
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction}
     * object.
     */
    public UnivariateFunction getMarginalLikelihood(String parname, FitState state, String... freePars) {
        ConfidenceLimitCalculator calculator;
        if (freePars.length == 0) {
            calculator = this.getCalculator(parname, state, state.getModel().namesAsArray());
        } else {
            calculator = this.getCalculator(parname, state, freePars);
        }
        return new UnivariateSplineWrapper(calculator.getProbability());
    }

    /**
     * Prints spline smoothed marginal likelihood for the parameter TODO нужно
     * сделать возможность контролировать количество точек кэширования
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param parname a {@link java.lang.String} object.
     * @param result a {@link hep.dataforge.datafitter.FitState} object.
     * @param freePars an array of {@link java.lang.String} objects.
     * @param numCalls a int.
     */
    public void printMarginalLikelihood(PrintWriter out, String parname, FitState result, String[] freePars, int numCalls) {
        ConfidenceLimitCalculator calculator = this.getCalculator(parname, result, freePars, numCalls);
        UnivariateFunction prob = calculator.getProbability();
        UnivariateFunction integr = calculator.getIntegralProbability();
        double[] grid = getUniformUnivariateGrid(calculator.a, calculator.b, 50);
        out.printf("%n*** The marginalized likelihood function for parameter \'%s\' ***%n%n", parname);
        out.printf("%-10s\t%-8s\t%-8s%n", parname, "Like", "Integral");
        for (int i = 0; i < grid.length; i++) {
            out.printf("%-10.8g\t%-8.8g\t%-8.8g%n", grid[i], prob.value(grid[i]), integr.value(grid[i]));

        }
        out.println();
    }
}
