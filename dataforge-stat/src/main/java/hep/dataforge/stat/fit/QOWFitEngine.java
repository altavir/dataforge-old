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

import hep.dataforge.io.reports.Report;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.maths.MathUtils;
import hep.dataforge.maths.NamedMatrix;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.utils.Utils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import static hep.dataforge.maths.MatrixOperations.inverse;
import static hep.dataforge.stat.fit.FitStage.*;

/**
 * <p>
 * QOWFitEngine class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class QOWFitEngine implements FitEngine {

    /**
     * Constant <code>QOW_ENGINE_NAME="QOW"</code>
     */
    public static final String QOW_ENGINE_NAME = "QOW";
    /**
     * Constant <code>QOW_METHOD_FAST="fast"</code>
     */
    public static final String QOW_METHOD_FAST = "fast";

//    /**
//     * <p>Constructor for QOWFitEngine.</p>
//     *
//     * @param report a {@link hep.dataforge.io.Reportable} object.
//     */
//    public QOWFitEngine(Reportable report) {
//        super(report);
//    }
    private ParamSet newtonianRun(FitState state, FitStage task, QOWeight weight, Reportable log) {
        int maxSteps = task.meta().getInt("iterations", 100);
        double tolerance = task.meta().getDouble("tolerance", 0);

        double dis, dis1; //норма невязки
        // Для удобства работаем всегда с полным набором параметров
        ParamSet par = state.getParameters().copy();
        ParamSet par1; //текущее решение

        log.report("Starting newtonian iteration from: \n\t{}",
                MathUtils.toString(par, weight.namesAsArray()));

        NamedVector eqvalues = QOWUtils.getEqValues(state, par, weight);//значения функций

        dis = eqvalues.getVector().getNorm();// невязка
        log.report("Starting discrepancy is {}", dis);
        int i = 0;
        boolean flag = false;
        while (!flag) {
            i++;
            log.report("Starting step number {}", i);

            if (task.getMethodName().equalsIgnoreCase(QOW_METHOD_FAST)) {
                //Берет значения матрицы в той точке, где считается вес
                par1 = fastNewtonianStep(state, par, eqvalues, weight);
            } else {
                //Берет значения матрицы в точке par
                par1 = newtonianStep(state, par, eqvalues, weight);
            }
            // здесь должен стоять учет границ параметров

            log.report("Parameter values after step are: \n\t{}",
                    MathUtils.toString(par1, weight.namesAsArray()));

            eqvalues = QOWUtils.getEqValues(state, par1, weight);
            dis1 = eqvalues.getVector().getNorm();// невязка после шага

            log.report("The discrepancy after step is: {}", dis1);
            if ((dis1 >= dis) && (i > 1)) {
                //дополнительно проверяем, чтобы был сделан хотя бы один шаг
                flag = true;
                log.report("The discrepancy does not decrease. Stopping iteration.");
            } else {
                par = par1;
                dis = dis1;
            }
            if (i >= maxSteps) {
                flag = true;
                log.report("Maximum number of iterations reached. Stopping iteration.");
            }
            if (dis <= tolerance) {
                flag = true;
                log.report("Tolerance threshold is reached. Stopping iteration.");
            }
        }

        return par;
    }

    private ParamSet newtonianStep(FitState source, ParamSet par, NamedVector eqvalues, QOWeight weight) {
        Utils.checkThread();// check if action is cacneled
        RealVector start = par.getParValues(weight.namesAsArray()).getVector();
        RealMatrix invJacob = inverse(QOWUtils.getEqDerivValues(source, par, weight));

        RealVector step = invJacob.operate(new ArrayRealVector(eqvalues.getArray()));
        return par.copy().setParValues(new NamedVector(weight.namesAsArray(), start.subtract(step)));
    }

    private ParamSet fastNewtonianStep(FitState source, ParamSet par, NamedVector eqvalues, QOWeight weight) {
        Utils.checkThread();// check if action is cacneled
        RealVector start = par.getParValues(weight.namesAsArray()).getVector();
        RealMatrix invJacob = inverse(QOWUtils.getEqDerivValues(source, weight));

        RealVector step = invJacob.operate(new ArrayRealVector(eqvalues.getArray()));
        return par.copy().setParValues(new NamedVector(weight.namesAsArray(), start.subtract(step)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FitTaskResult run(FitState state, FitStage task, Reportable parentLog) {
        Report log = new Report("QOW", parentLog);
        log.report("QOW fit engine started task '{}'", task.getName());
        switch (task.getName()) {
            case TASK_SINGLE:
                return makeRun(state, task, log);
            case TASK_COVARIANCE:
                return generateErrors(state, task, log);
            case TASK_RUN:
                FitState res = makeRun(state, task, log);
                res = makeRun(res, task, log);
                return generateErrors(res, task, log);
            default:
                throw new IllegalArgumentException("Unknown task");
        }
    }

    private FitTaskResult makeRun(FitState state, FitStage task, Reportable log) {
        /*Инициализация объектов, задание исходных значений*/
        log.report("Starting fit using quasioptimal weights method.");

        String[] fitPars = getFitPars(state, task);

        QOWeight curWeight = new QOWeight(state, fitPars, state.getParameters());

        // вычисляем вес в allPar. Потом можно будет попробовать ручное задание веса
        log.report("The starting weight is: \n\t{}",
                MathUtils.toString(curWeight.getTheta()));

        //Стартовая точка такая же как и параметр веса
        /*Фитирование*/
        ParamSet res = this.newtonianRun(state, task, curWeight, log);

        /*Генерация результата*/
        FitTaskResult result = FitTaskResult.buildResult(state, task, res);

        return result;
    }

    /**
     * <p>
     * generateErrors.</p>
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     * @param task a {@link hep.dataforge.stat.fit.FitStage} object.
     * @param log a {@link hep.dataforge.io.reports.Reportable} object.
     * @return a {@link hep.dataforge.stat.fit.FitTaskResult} object.
     */
    public FitTaskResult generateErrors(FitState state, FitStage task, Reportable log) {

        log.report("Starting errors estimation using quasioptimal weights method.");

        String[] fitPars = getFitPars(state, task);

        QOWeight curWeight = new QOWeight(state, fitPars, state.getParameters());

        // вычисляем вес в allPar. Потом можно будет попробовать ручное задание веса
        log.report("The starting weight is: \n\t{}",
                MathUtils.toString(curWeight.getTheta()));

//        ParamSet pars = state.getParameters().copy();
        NamedMatrix covar = getCovariance(state, curWeight);

        FitTaskResult result = FitTaskResult.buildResult(state, task, covar);
        EigenDecomposition decomposition = new EigenDecomposition(covar.getMatrix());
        for (double lambda : decomposition.getRealEigenvalues()) {
            if (lambda <= 0) {
                log.report("The covariance matrix is not positive defined. Error estimation is not valid");
                result.setValid(false);
            }
        }

        return result;

    }

    private NamedMatrix getCovariance(FitState source, QOWeight weight) {
//        RealMatrix res;
        RealMatrix invH = inverse(QOWUtils.getEqDerivValues(source, weight.namesAsArray(), weight));
//        RealMatrix transinvH = invH.transpose();
//        RealMatrix covarF = QOWUtils.covarF(source, weight);
//        res = invH.multiply(covarF).multiply(transinvH);

//        return new NamedMatrix(res, weight.asArray());
        return new NamedMatrix(invH, weight.namesAsArray());
    }

}
