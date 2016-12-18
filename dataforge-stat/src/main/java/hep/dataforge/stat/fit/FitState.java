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

import hep.dataforge.io.FittingIOUtils;
import hep.dataforge.maths.NamedMatrix;
import hep.dataforge.stat.likelihood.LogLikelihood;
import hep.dataforge.stat.models.Model;
import hep.dataforge.stat.parametric.DerivativeCalculator;
import hep.dataforge.stat.parametric.ParametricValue;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.Table;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.PrintWriter;
import java.io.Serializable;

import static hep.dataforge.io.FittingIOUtils.printParamSet;
import static org.apache.commons.math3.util.MathArrays.ebeMultiply;

/**
 * This class combine the information required to fit data. The key elements are
 * Table, Model and initial ParamSet. Additionally, one can provide
 * covariance matrix, prior probability, fit history etc. To simplify
 * construction of FitState use FitState.Builder
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
//TODO добавить параметры по-умолчанию в модель
public class FitState implements Serializable {

    private final Table dataSet;

    private final Model model;

    private final ParametricValue prior;

    private final NamedMatrix covariance;

    private final IntervalEstimate interval;

    private final ParamSet pars;

    public FitState(Table dataSet, Model model, ParamSet pars) {
        this.dataSet = dataSet;
        this.model = model;
        this.prior = null;
        this.pars = pars;
        this.covariance = null;
        this.interval = null;
    }

    public FitState(Table dataSet, Model model, ParamSet pars,
                    NamedMatrix covariance, IntervalEstimate interval, ParametricValue prior) {
        this.dataSet = dataSet;
        this.model = model;
        this.prior = prior;
        this.covariance = covariance;
        this.interval = interval;
        this.pars = pars;
    }

    /**
     * clone constructor
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     */
    protected FitState(FitState state) {
        this.dataSet = state.getDataSet();
        this.model = state.getModel();
        this.prior = state.getPrior();
        this.covariance = state.covariance;
        this.pars = state.pars;
        this.interval = state.interval;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates new FitState object based on this one and returns its Builder.
     *
     * @return
     */
    public Builder edit() {
        return new Builder(this);
    }

    public double getChi2() {
        return getChi2(pars);
    }

    public NamedMatrix getCorrelationMatrix() {
        if (covariance == null || pars == null) {
            return null;
        }
        NamedMatrix res = covariance.copy();
        String[] names = covariance.namesAsArray();

        for (String str1 : names) {
            for (String str2 : names) {
                double value = res.getElement(str1, str2) / pars.getError(str1) / pars.getError(str2);
                res.setElement(str1, str2, value);
            }
        }
        return res;
    }

    /**
     * Возвращается всегда полная матрица, включающая даже параметры, которые не
     * фитировались. Для параметров, для которых нет матрицы в явном виде
     * возвращаются только диоганальные элементы.
     *
     * @return the covariance
     */
    public NamedMatrix getCovariance() {
        double[] sigmas = this.pars.getParErrors().getArray();
        sigmas = ebeMultiply(sigmas, sigmas);
        RealMatrix baseMatrix = new DiagonalMatrix(sigmas);
        NamedMatrix result = new NamedMatrix(baseMatrix, this.pars.namesAsArray());
        if (hasCovariance()) {
            result.setValuesFrom(this.covariance);
        }
        return result;
    }

    /**
     * Shows if state has defined covariance. Otherwise singular covariance is used
     * @return
     */
    public boolean hasCovariance() {
        return covariance != null;
    }

    public IntervalEstimate getIntervalEstimate() {
        return this.interval;
    }

    public ParamSet getParameters() {
        return pars;
    }

    public void print(PrintWriter out) {
        out.println("***FITTING RESULT***");
        this.printAllValues(out);
        if (covariance != null) {
            out.println();
            out.println("Correlation marix:");
            FittingIOUtils.printNamedMatrix(out, getCorrelationMatrix());
        }
        if (this.interval != null) {
            this.interval.print(out);
        }
        out.println();
        double chi2 = getChi2();
        out.printf("Chi squared: %g%n", chi2);
        out.println();
        out.flush();
    }

    protected void printAllValues(PrintWriter out) {
        out.println();
        out.println("All function parameters are: ");
        printParamSet(out, this.pars);
    }

    private void printCovariance(PrintWriter out) {
        if (getCovariance() != null) {
            out.println();
            out.printf("%n***COVARIANCE***%n");

            FittingIOUtils.printNamedMatrix(out, getCovariance());

        }
    }

    /**
     * Априорная вероятность не учитывается
     *
     * @param set a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @return a double.
     */
    public double getChi2(ParamSet set) {
        int i;
        double res = 0;
        double d;
        double s;
        for (i = 0; i < this.getDataSize(); i++) {
            d = this.getDis(i, set);
            s = this.getDispersion(i, set);
            res += d * d / s;
        }
        return res;
    }

    /**
     * Возвращает расстояния от i-той точки до спектра с параметрами pars.
     * расстояние в общем случае идет со знаком и для одномерного случая
     * описыватьеся как спектр-данные.
     *
     * @param i    a int.
     * @param pars a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @return a double.
     */
    public double getDis(int i, ParamSet pars) {
        return model.distance(dataSet.getRow(i), pars);
    }

    /**
     * Производная от расстояния по параметру "name". Совпадает с производной
     * исходной функции в одномерном случае На этом этапе обабатывается
     * {@code NotDefinedException}. В случае обращения, производная вычисляется
     * внутренним калькулятором.
     *
     * @param name a {@link java.lang.String} object.
     * @param i    a int.
     * @param pars a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @return a double.
     */
    public double getDisDeriv(final String name, final int i, final ParamSet pars) {
        DataPoint dp = dataSet.getRow(i);
        if (model.providesDeriv(name)) {
            return model.disDeriv(name, dp, pars);
        } else {
            return DerivativeCalculator.calculateDerivative(model.getDistanceFunction(dp), pars, name);
        }
    }

    /**
     * Дисперсия i-той точки. В одномерном случае квадрат ошибки. Значения
     * параметров передаются на всякий случай, если вдруг придется делать
     * зависимость веса от параметров.
     *
     * @param i    a int.
     * @param pars a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @return a double.
     */
    public double getDispersion(int i, ParamSet pars) {
        double res = model.dispersion(dataSet.getRow(i), pars);
        if (res > 0) {
            return res;
        } else {
            throw new RuntimeException("The returned weight of a data point is infinite. Can not proceed because of infinite point significance.");
        }
    }

    /**
     * Учитывается вероятность, заданная в модели и априорная вероятность
     *
     * @param set a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @return a double.
     */
    public double getLogProb(ParamSet set) {
        double res = 0;
        if (!model.providesProb()) {
            res = -getChi2(set) / 2;
        } else {
            for (DataPoint dp : dataSet) {
                res += model.getLogProb(dp, set);
            }
        }
        if (getPrior() != null) {
            //логарифм произведения равен сумме логарифмов
            res += Math.log(getPrior().apply(set));
        }
        return res;
    }

    /**
     * Учитывается вероятность, заданная в модели и априорная вероятность
     *
     * @param parName a {@link java.lang.String} object.
     * @param set     a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @return a double.
     */
    public double getLogProbDeriv(String parName, ParamSet set) {
        double res = 0;
        if (!model.providesProbDeriv(parName)) {
            double d;
            double s;
            double deriv;
            for (int i = 0; i < getDataSize(); i++) {
                d = getDis(i, set);
                s = getDispersion(i, set);
                deriv = getDisDeriv(parName, i, set);
                res -= d * deriv / s;
            }
        } else {
            for (DataPoint dp : dataSet) {
                res += model.getLogProbDeriv(parName, dp, set);
            }
        }
        if ((getPrior() != null) && (getPrior().names().contains(parName))) {
            return res += getPrior().derivValue(parName, set) / getPrior().apply(set);
        }
        return res;
    }

    public boolean providesValidAnalyticalDerivs(ParamSet set, String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException();
        }
        ParametricValue like = this.getLogLike();
        for (String name : names) {
            if (!this.modelProvidesDerivs(name)) {
                return false;
            }
            if (!DerivativeCalculator.providesValidDerivative(like, set, 1e-1, name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Возвращает информацию о том, возвращает ли МОДЕЛЬ производные.
     * FitDataSource при этом может возвращать производные в любом случае.
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean modelProvidesDerivs(String name) {
        return this.model.providesDeriv(name);
    }

    public LogLikelihood getLogLike() {
        return new LogLikelihood(this);
    }

    public ParametricValue getPrior() {
        return prior;
    }

    public Model getModel() {
        return model;
    }

    public int getModelDim() {
        return model.size();
    }

    public Table getDataSet() {
        return dataSet;
    }

    public int getDataSize() {
        return dataSet.size();
    }

    /**
     *
     */
    public static class Builder {

        private Table dataSet;
        private IntervalEstimate interval;
        private Model model;
        private ParamSet pars;
        private NamedMatrix covariance;
        private ParametricValue prior;

        public Builder(FitState state) {
            this.covariance = state.covariance;
            this.dataSet = state.dataSet;
            this.interval = state.interval;
            this.model = state.model;
            this.pars = state.pars;
            this.prior = state.prior;
        }

        public Builder() {

        }

        public Builder setDataSet(Table dataSet) {
            this.dataSet = dataSet;
            return this;
        }

        public Builder setModel(Model model) {
            this.model = model;
            return this;
        }

        public Builder setPars(ParamSet pars) {
            this.pars = pars;
            return this;
        }

        public Builder setCovariance(NamedMatrix cov, boolean updateErrors) {
            covariance = cov;
            if (updateErrors) {
                for (String name : cov.names()) {
                    double value = cov.getElement(name, name);
                    if (value > 0) {
                        pars.setParError(name, Math.sqrt(value));
                    } else {
                        throw new IllegalArgumentException("The argument is not valid covariance");
                    }
                }
            }
            return this;
        }

        public Builder setPrior(ParametricValue priorDistribution) {
            prior = priorDistribution;
            return this;
        }

        public Builder setInterval(IntervalEstimate intervalEstimate) {
            this.interval = intervalEstimate;
            return this;
        }

        public FitState build() {
            if (dataSet == null || model == null || pars == null) {
                throw new IllegalStateException("Can't build FitState, data, model and starting parameters must be provided.");
            }
            return new FitState(dataSet, model, pars, covariance, interval, prior);
        }

    }

}
