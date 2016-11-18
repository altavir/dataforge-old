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
package hep.dataforge.stat.fit;

import hep.dataforge.io.reports.Log;
import hep.dataforge.io.reports.Logable;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.stat.parametric.FunctionUtils;
import hep.dataforge.stat.parametric.MultiFunction;
import hep.dataforge.stat.parametric.ParametricMultiFunctionWrapper;
import hep.dataforge.stat.parametric.ParametricValue;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.AbstractSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import static hep.dataforge.maths.RandomUtils.getDefaultRandomGenerator;
import static hep.dataforge.stat.fit.FitStage.TASK_RUN;
import static hep.dataforge.stat.fit.FitStage.TASK_SINGLE;
import static java.lang.Math.log;

/**
 * <p>
 * CMFitEngine class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class CMFitEngine implements FitEngine {

    /**
     * Constant <code>CM_ENGINE_NAME="CM"</code>
     */
    public static final String CM_ENGINE_NAME = "CM";
    /**
     * Constant <code>CM_NELDERMEADSIMPLEX="neldermead"</code>
     */
    public static final String CM_NELDERMEADSIMPLEX = "neldermead";
    /**
     * Constant <code>CM_CMAESO="CMAESO"</code>
     */
    public static final String CM_CMAESO = "CMAESO";

    private final static int DEFAULT_MAXITER = 1000;
    private final static double DEFAULT_TOLERANCE = 1e-5;

    /** {@inheritDoc} */
    @Override
    public FitTaskResult run(FitState state, FitStage task, Logable parentLog) {
        Log log = new Log("CM", parentLog);
        switch (task.getName()) {
            case TASK_SINGLE:
            case TASK_RUN:
                return makeRun(state, task, log);
            default:
                throw new IllegalArgumentException(String.format("Task '%s' is not supported by CMFitEngine", task.getName()));
        }
    }

    /**
     * <p>
     * makeRun.</p>
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     * @param task a {@link hep.dataforge.stat.fit.FitStage} object.
     * @param log a {@link Logable} object.
     * @return a {@link hep.dataforge.stat.fit.FitTaskResult} object.
     */
    public FitTaskResult makeRun(FitState state, FitStage task, Logable log) {

        log.report("Starting fit using provided Commons Math algorithms.");
        int maxSteps = task.meta().getInt("iterations", DEFAULT_MAXITER);
        double tolerance = task.meta().getDouble("tolerance", DEFAULT_TOLERANCE);
        String[] fitPars = this.getFitPars(state, task);
        ParamSet pars = state.getParameters().copy();

        NamedVector subSet = pars.getParValues(fitPars);
        ParametricValue likeFunc = FunctionUtils.getNamedSubFunction(state.getLogLike(), pars, fitPars);

        MultiFunction func = new ParametricMultiFunctionWrapper(likeFunc);
        ObjectiveFunction oFunc;
        MaxEval maxEval = new MaxEval(maxSteps);
        InitialGuess ig = new InitialGuess(subSet.getArray());

        double[] upBounds = new double[fitPars.length];
        double[] loBounds = new double[fitPars.length];

        for (int i = 0; i < fitPars.length; i++) {
            Param p = pars.getByName(fitPars[i]);
            upBounds[i] = p.getUpperBound();
            loBounds[i] = p.getLowerBound();
        }

        PointValuePair res;
        SimpleValueChecker checker = new SimpleValueChecker(tolerance, 0);

        switch (task.getMethodName()) {
            case CM_NELDERMEADSIMPLEX:
                log.report("Using Nelder Mead Simlex (no derivs).");
                AbstractSimplex simplex = new NelderMeadSimplex(pars.getParErrors(fitPars).getArray());
                MultivariateOptimizer nmOptimizer = new SimplexOptimizer(checker);

                oFunc = new ObjectiveFunction(new MultivariateFunctionMappingAdapter(func, loBounds, upBounds));
                res = nmOptimizer.optimize(oFunc, maxEval, ig, GoalType.MAXIMIZE, simplex);
                break;
            default:
                log.report("Using CMAESO optimizer (no derivs).");
                SimpleBounds sb = new SimpleBounds(loBounds, upBounds);
                MultivariateOptimizer CMAESOoptimizer = new CMAESOptimizer(100, Double.NEGATIVE_INFINITY,
                        true, 4, 4, getDefaultRandomGenerator(), false, checker);

                CMAESOptimizer.Sigma sigmas = new CMAESOptimizer.Sigma(pars.getParErrors(fitPars).getArray());
                CMAESOptimizer.PopulationSize popSize
                        = new CMAESOptimizer.PopulationSize((int) (4 + 3 * log(fitPars.length)));

                oFunc = new ObjectiveFunction(func);
                res = CMAESOoptimizer.optimize(oFunc, maxEval, ig, sb, sigmas, popSize, GoalType.MAXIMIZE);
                break;
        }

        NamedVector respars = new NamedVector(fitPars, res.getPoint());
        ParamSet allpars = pars.copy();
        allpars.setParValues(respars);
        FitTaskResult outRes = FitTaskResult.buildResult(state, task, allpars);

        return outRes;

    }

}
