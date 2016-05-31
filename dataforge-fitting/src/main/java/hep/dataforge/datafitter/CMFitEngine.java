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
package hep.dataforge.datafitter;

import static hep.dataforge.datafitter.FitTask.TASK_RUN;
import static hep.dataforge.datafitter.FitTask.TASK_SINGLE;
import hep.dataforge.functions.FunctionUtils;
import hep.dataforge.functions.MultiFunction;
import hep.dataforge.functions.NamedFunction;
import hep.dataforge.functions.NamedMultiFunction;
import hep.dataforge.io.reports.Report;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.maths.NamedVector;
import static hep.dataforge.maths.RandomUtils.getDefaultRandomGenerator;
import static java.lang.Math.log;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.AbstractSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

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
    public FitTaskResult run(FitState state, FitTask task, Reportable parentLog) {
        Report log = new Report("CM", parentLog);
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
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param task a {@link hep.dataforge.datafitter.FitTask} object.
     * @param log a {@link hep.dataforge.io.reports.Reportable} object.
     * @return a {@link hep.dataforge.datafitter.FitTaskResult} object.
     */
    public FitTaskResult makeRun(FitState state, FitTask task, Reportable log) {

        log.report("Starting fit using provided Commons Math algorithms.");
        int maxSteps = task.meta().getInt("iterations", DEFAULT_MAXITER);
        double tolerance = task.meta().getDouble("tolerance", DEFAULT_TOLERANCE);
        String[] fitPars = this.getFitPars(state, task);
        ParamSet pars = state.getParameters().copy();

        NamedVector subSet = pars.getParValues(fitPars);
        NamedFunction likeFunc = FunctionUtils.getNamedSubFunction(state.getLogLike(), pars, fitPars);

        MultiFunction func = new NamedMultiFunction(likeFunc);
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
