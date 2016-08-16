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

import hep.dataforge.MINUIT.*;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.io.reports.Report;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.maths.NamedMatrix;
import hep.dataforge.stat.parametric.MultiFunction;

import static hep.dataforge.stat.fit.FitStage.*;

/**
 * <p>
 * MINUITFitEngine class.</p>
 *
 * @author Darksnake
 * @version $Id: $Id
 */
public class MINUITFitEngine implements FitEngine {

    /**
     * Constant <code>MINUIT_MIGRAD="MIGRAD"</code>
     */
    public static final String MINUIT_MIGRAD = "MIGRAD";
    /**
     * Constant <code>MINUIT_MINIMIZE="MINIMIZE"</code>
     */
    public static final String MINUIT_MINIMIZE = "MINIMIZE";
    /**
     * Constant <code>MINUIT_SIMPLEX="SIMPLEX"</code>
     */
    public static final String MINUIT_SIMPLEX = "SIMPLEX";
    /**
     * Constant <code>MINUIT_MINOS="MINOS"</code>
     */
    public static final String MINUIT_MINOS = "MINOS";//MINOS errors
    /**
     * Constant <code>MINUIT_HESSE="HESSE"</code>
     */
    public static final String MINUIT_HESSE = "HESSE";//HESSE errors

    /**
     * Constant <code>MINUIT_ENGINE_NAME="MINUIT"</code>
     */
    public static final String MINUIT_ENGINE_NAME = "MINUIT";

    public MINUITFitEngine() {

    }

    /**
     * {@inheritDoc}
     *
     * @param parentLog
     */
    @Override
    public FitTaskResult run(FitState state, FitStage task, Reportable parentLog) {
        Report log = new Report("MINUIT", parentLog);

        log.report("MINUIT fit engine started task '{}'", task.getName());
        switch (task.getName()) {
            case TASK_COVARIANCE:
                return runHesse(state, task, log);
            case TASK_SINGLE:
            case TASK_RUN:
                return runFit(state, task, log);
            default:
                throw new IllegalArgumentException("Unknown task");
        }
    }

    /**
     * <p>
     * runHesse.</p>
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     * @param task a {@link hep.dataforge.stat.fit.FitStage} object.
     * @param log
     * @return a {@link hep.dataforge.stat.fit.FitTaskResult} object.
     */
    public FitTaskResult runHesse(FitState state, FitStage task, Reportable log) {
        int strategy;
        strategy = GlobalContext.instance().getInt("MINUIT_STRATEGY", 2);

        log.report("Generating errors using MnHesse 2-nd order gradient calculator.");

        MultiFunction fcn;
        String[] fitPars = getFitPars(state, task);
        ParamSet pars = state.getParameters();

        fcn = MINUITUtils.getFcn(state, pars, fitPars);

        MnHesse hesse = new MnHesse(strategy);

        MnUserParameterState mnState = hesse.calculate(fcn, MINUITUtils.getFitParameters(pars, fitPars));

        ParamSet allPars = pars.copy();
        for (String fitPar : fitPars) {
            allPars.setParValue(fitPar, mnState.value(fitPar));
            allPars.setParError(fitPar, mnState.error(fitPar));
        }

        FitState.Builder newState = state.edit();
        newState.setPars(allPars);

        if (mnState.hasCovariance()) {
            MnUserCovariance mnCov = mnState.covariance();
            int j;
            double[][] cov = new double[mnState.variableParameters()][mnState.variableParameters()];

            for (int i = 0; i < mnState.variableParameters(); i++) {
                for (j = 0; j < mnState.variableParameters(); j++) {
                    cov[i][j] = mnCov.get(i, j);
                }
            }
            newState.setCovariance(new NamedMatrix(cov, fitPars), true);

        }

        return new FitTaskResult(newState.build(), task);
    }

    /**
     * <p>
     * runFit.</p>
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     * @param task a {@link hep.dataforge.stat.fit.FitStage} object.
     * @param log
     * @return a {@link hep.dataforge.stat.fit.FitTaskResult} object.
     */
    public FitTaskResult runFit(FitState state, FitStage task, Reportable log) {

        MnApplication minuit;
        log.report("Starting fit using Minuit.");

        int strategy;
        strategy = GlobalContext.instance().getInt("MINUIT_STRATEGY", 2);
        boolean force;
        force = GlobalContext.instance().getBoolean("FORCE_DERIVS", false);

        String[] fitPars = getFitPars(state, task);

        for (String fitPar : fitPars) {
            if (!state.modelProvidesDerivs(fitPar)) {
                force = true;
                log.getLogger().debug("Model does not provide derivatives for parameter '{}'", fitPar);
            }
        }
        if(force){
            log.report("Using MINUIT gradient calculator.");
        }

        MultiFunction fcn;

        ParamSet pars = state.getParameters().copy();
        fcn = MINUITUtils.getFcn(state, pars, fitPars);

        switch (task.getMethodName()) {
            case MINUIT_MINOS:    // Для миноса используем универсальный алгоритм
            case MINUIT_MINIMIZE:
                minuit = new MnMinimize(fcn, MINUITUtils.getFitParameters(pars, fitPars), strategy);
                break;
            case MINUIT_SIMPLEX:
                minuit = new MnSimplex(fcn, MINUITUtils.getFitParameters(pars, fitPars), strategy);
                break;
            default:
                minuit = new MnMigrad(fcn, MINUITUtils.getFitParameters(pars, fitPars), strategy);
        }

        if (force) {
            minuit.setUseAnalyticalDerivatives(false);
            log.report("Forced to use MINUIT internal derivative calculator!");
        }

//        minuit.setUseAnalyticalDerivatives(true);
        FunctionMinimum minimum;

        int maxSteps = task.meta().getInt("iterations", -1);
        double tolerance = task.meta().getDouble("tolerance", -1);

        if (maxSteps > 0) {
            if (tolerance > 0) {
                minimum = minuit.minimize(maxSteps, tolerance);
            } else {
                minimum = minuit.minimize(maxSteps);
            }
        } else {
            minimum = minuit.minimize();
        }
        if (!minimum.isValid()) {
            log.report("Minimization failed!");
        }
        log.report("MINUIT run completed in {} function calls.", minimum.nfcn());

        /*
         * Генерация результата
         */
        ParamSet allPars = pars.copy();
        for (String fitPar : fitPars) {
            allPars.setParValue(fitPar, minimum.userParameters().value(fitPar));
            allPars.setParError(fitPar, minimum.userParameters().error(fitPar));
        }

        FitState.Builder newState = state.edit();
        newState.setPars(allPars);

        boolean valid = minimum.isValid();

        if (minimum.userCovariance().nrow() > 0) {
            int j;
            double[][] cov = new double[minuit.variableParameters()][minuit.variableParameters()];
            if (cov[0].length == 1) {
                cov[0][0] = minimum.userParameters().error(0) * minimum.userParameters().error(0);
            } else {
                for (int i = 0; i < minuit.variableParameters(); i++) {
                    for (j = 0; j < minuit.variableParameters(); j++) {
                        cov[i][j] = minimum.userCovariance().get(i, j);
                    }
                }
            }
            newState.setCovariance(new NamedMatrix(cov, fitPars), true);

        }

        if (task.getMethodName().equals(MINUIT_MINOS)) {
            log.report("Starting MINOS procedure for precise error estimation.");
            MnMinos minos = new MnMinos(fcn, minimum, strategy);
            MinosError mnError;
            double[] errl = new double[fitPars.length];
            double[] errp = new double[fitPars.length];
            for (int i = 0; i < fitPars.length; i++) {
                mnError = minos.minos(i);
                if (mnError.isValid()) {
                    errl[i] = mnError.lower();
                    errp[i] = mnError.upper();
                } else {
                    valid = false;
                }
            }
            MINOSResult minosErrors = new MINOSResult(fitPars, errl, errp);
            newState.setInterval(minosErrors);
        }

        FitTaskResult result = new FitTaskResult(newState.build(), task);
        result.setValid(valid);
        return result;

    }

}
