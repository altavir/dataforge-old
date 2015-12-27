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
package hep.dataforge.io;

import hep.dataforge.data.DataPoint;
import hep.dataforge.data.ListDataSet;
import hep.dataforge.data.XYDataAdapter;
import hep.dataforge.datafitter.FitState;
import hep.dataforge.datafitter.Param;
import hep.dataforge.datafitter.ParamSet;
import hep.dataforge.datafitter.models.Model;
import hep.dataforge.datafitter.models.XYModel;
import hep.dataforge.functions.Function;
import hep.dataforge.functions.FunctionUtils;
import hep.dataforge.functions.NamedFunction;
import hep.dataforge.functions.ParametricFunction;
import static hep.dataforge.io.OutputData.getNamedFunctionData;
import static hep.dataforge.io.OutputData.printDataSet;
import static hep.dataforge.io.PrintFunction.printFunctionSimple;
import hep.dataforge.likelihood.LogLikelihood;
import hep.dataforge.maths.GridCalculator;
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.maths.NamedMatrix;
import java.io.PrintWriter;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.RealMatrix;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * <p>
 * PrintNamed class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class PrintNamed {

    /**
     * Выводит на печать значения прадвоподобия (с автоматическим
     * масштабированием) по двум параметрам. Сначала идет перебор по параметру
     * {@code par1}, потом по {@code par2}.
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param head a {@link java.lang.String} object.
     * @param res a {@link hep.dataforge.datafitter.FitState} object.
     * @param par1 a {@link java.lang.String} object.
     * @param par2 a {@link java.lang.String} object.
     * @param num1 a int.
     * @param num2 a int.
     * @param scale - на сколько ошибоку нужно отступать от максимума
     */
    public static void printLike2D(PrintWriter out, String head, FitState res, String par1, String par2, int num1, int num2, double scale) {

        double val1 = res.getParameters().getValue(par1);
        double val2 = res.getParameters().getValue(par2);
        double err1 = res.getParameters().getError(par1);
        double err2 = res.getParameters().getError(par2);

        double[] grid1 = GridCalculator.getUniformUnivariateGrid(val1 - scale * err1, val1 + scale * err1, num1);
        double[] grid2 = GridCalculator.getUniformUnivariateGrid(val2 - scale * err2, val2 + scale * err2, num2);

        LogLikelihood like = res.getLogLike();
        like.reScale(res.getParameters());
        NamedFunction func = FunctionUtils.getNamedSubFunction(like.getLikelihood(), res.getParameters(), par1, par2);

        double[] vector = new double[2];

        String[] names = {par1, par2};

        ArrayList<NamedDoubleArray> points = new ArrayList<>();

        for (double x : grid1) {
            vector[0] = x;
            for (double y : grid2) {
                vector[1] = y;
                points.add(new NamedDoubleArray(names, vector));
            }
        }

        ListDataSet data = getNamedFunctionData(func, points);

        printDataSet(out, data, head);
    }

    /**
     * <p>
     * printLogProb1D.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param res a {@link hep.dataforge.datafitter.FitState} object.
     * @param numpoints a int.
     * @param scale a double.
     * @param name a {@link java.lang.String} object.
     */
    public static void printLogProb1D(PrintWriter out, FitState res, int numpoints, double scale, String name) {
        LogLikelihood like = res.getLogLike();
        Function func = FunctionUtils.getNamedProjection(like, name, res.getParameters());
        Param p = res.getParameters().getByName(name);
        double a = max(p.value() - scale * p.getErr(), p.getLowerBound());
        double b = min(p.value() + scale * p.getErr(), p.getUpperBound());
        printFunctionSimple(out, func, a, b, numpoints);
    }

    /**
     * Использует информацию об ошибках для определения региона. И случайный
     * гауссовский генератор
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param head a {@link java.lang.String} object.
     * @param res a {@link hep.dataforge.datafitter.FitState} object.
     * @param numpoints a int.
     * @param scale a double.
     * @param names a {@link java.lang.String} object.
     */
    public static void printLogProbRandom(PrintWriter out, String head, FitState res, int numpoints, double scale, String... names) {

        assert names.length > 0;
        LogLikelihood like = res.getLogLike();
        NamedFunction func = FunctionUtils.getNamedSubFunction(like, res.getParameters(), names);

        double[] vals = res.getParameters().getParValues(names).getValues();

        NamedMatrix fullCov = res.getCovariance();
        RealMatrix reducedCov = fullCov.getNamedSubMatrix(names).getMatrix().scalarMultiply(scale);
        MultivariateNormalDistribution distr
                = new MultivariateNormalDistribution(vals, reducedCov.getData());

        ArrayList<NamedDoubleArray> points = new ArrayList<>();

        for (int i = 0; i < numpoints; i++) {
            points.add(new NamedDoubleArray(names, distr.sample()));
        }

        ListDataSet data = getNamedFunctionData(func, points);

        printDataSet(out, data, head);
    }

    /**
     * <p>
     * printNamedMatrix.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param matrix a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public static void printNamedMatrix(PrintWriter out, NamedMatrix matrix) {
        out.println();

        String[] nameList = matrix.namesAsArray();
        for (String nameList1 : nameList) {
            out.printf("%-10s\t", nameList1);
        }

        out.println();
        out.println();

        for (int i = 0; i < nameList.length; i++) {
            for (int j = 0; j < nameList.length; j++) {
                out.printf("%10g\t", matrix.getMatrix().getEntry(i, j));

            }
            out.println();
        }

        out.println();
    }

    /**
     * <p>
     * printResiduals.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     */
    public static void printResiduals(PrintWriter out, FitState state) {
        printResiduals(out, state.getModel(), state.getDataSet(), state.getParameters());
    }

    /**
     * <p>
     * printResiduals.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param model a {@link hep.dataforge.datafitter.models.Model} object.
     * @param data a {@link java.lang.Iterable} object.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public static void printResiduals(PrintWriter out, Model model, Iterable<DataPoint> data, ParamSet pars) {
        out.println();// можно тут вставить шапку
        out.printf("residual\tsigma%n%n");
        for (DataPoint dp : data) {
            double dif = model.distance(dp, pars);
            double sigma = sqrt(model.dispersion(dp, pars));
            out.printf("%g\t%g%n", dif / sigma, sigma);
        }
        out.flush();
    }

    /**
     * <p>
     * printSpectrum.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param sp a {@link hep.dataforge.functions.ParametricFunction} object.
     * @param pars a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @param a a double.
     * @param b a double.
     * @param numPoints a int.
     */
    public static void printSpectrum(PrintWriter out, ParametricFunction sp, NamedDoubleSet pars, double a, double b, int numPoints) {
        UnivariateFunction func = FunctionUtils.getSpectrumFunction(sp, pars);
        printFunctionSimple(out, func, a, b, numPoints);
        out.flush();
    }

    /**
     * <p>
     * printSpectrumResiduals.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param model a {@link hep.dataforge.datafitter.models.XYModel}
     * object.
     * @param data a {@link java.lang.Iterable} object.
     * @param pars a {@link hep.dataforge.maths.NamedDoubleSet} object.
     */
    public static void printSpectrumResiduals(PrintWriter out, XYModel model, Iterable<DataPoint> data, NamedDoubleSet pars) {
        printSpectrumResiduals(out, model.getSpectrum(), data, model.getAdapter(), pars);
    }

    /**
     * <p>
     * printSpectrumResiduals.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param spectrum a {@link hep.dataforge.functions.ParametricFunction} object.
     * @param data a {@link java.lang.Iterable} object.
     * @param adapter a
     * {@link hep.dataforge.data.XYDataAdapter} object.
     * @param pars a {@link hep.dataforge.maths.NamedDoubleSet} object.
     */
    public static void printSpectrumResiduals(PrintWriter out, ParametricFunction spectrum,
            Iterable<DataPoint> data, XYDataAdapter adapter, NamedDoubleSet pars) {
        out.println();// можно тут вставить шапку
        out.printf("%8s\t%8s\t%8s\t%8s\t%8s%n", "x", "data", "error", "fit", "residual");

        for (DataPoint dp : data) {
            double x = adapter.getX(dp).doubleValue();
            double y = adapter.getY(dp).doubleValue();
            double sigma = adapter.getYerr(dp).doubleValue();

            double value = spectrum.value(x, pars);
            double dif = -(value - y) / sigma;

            out.printf("%8g\t%8g\t%8g\t%8g\t%8g%n", x, y, sigma, value, dif);
        }
        out.flush();
    }

}
