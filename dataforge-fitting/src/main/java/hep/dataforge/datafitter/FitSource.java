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

import hep.dataforge.datafitter.models.Model;
import hep.dataforge.content.AbstractContent;
import hep.dataforge.meta.Meta;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.functions.DerivativeCalculator;
import hep.dataforge.functions.NamedFunction;
import hep.dataforge.likelihood.LogLikelihood;

/**
 * <p>FitSource class.</p>
 *
 * @author Alexander Nozik
 */
public class FitSource extends AbstractContent {

    /**
     *
     */
    protected final DataSet dataSet;

    /**
     *
     */
    protected final Model model;

    /**
     *
     */
    protected final NamedFunction prior;

    /**
     * <p>Constructor for FitSource.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param dataSet a {@link hep.dataforge.data.DataSet} object.
     * @param model a {@link hep.dataforge.datafitter.models.Model} object.
     * @param prior a {@link hep.dataforge.functions.NamedFunction} object.
     */
    public FitSource(String name, Meta annotation, DataSet dataSet, Model model, NamedFunction prior) {
        super(name, annotation);
        this.dataSet = dataSet;
        this.model = model;
        this.prior = prior;
    }

    /**
     * <p>Constructor for FitSource.</p>
     *
     * @param dataSet a {@link hep.dataforge.data.DataSet} object.
     * @param model a {@link hep.dataforge.datafitter.models.Model} object.
     */
    public FitSource(DataSet dataSet, Model model) {
        this(null, null, dataSet, model, null);
    }

    /**
     * Априорная вероятность не учитывается
     *
     * @param set a {@link hep.dataforge.datafitter.ParamSet} object.
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
     * @param i a int.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a double.
     */
    public double getDis(int i, ParamSet pars) {
        return model.distance(dataSet.get(i), pars);
    }

    /**
     * Производная от расстояния по параметру "name". Совпадает с производной
     * исходной функции в одномерном случае На этом этапе обабатывается
     * {@code NotDefinedException}. В случае обращения, производная вычисляется
     * внутренним калькулятором.
     *
     * @param name a {@link java.lang.String} object.
     * @param i a int.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a double.
     */
    public double getDisDeriv(final String name, final int i, final ParamSet pars) {
        DataPoint dp = dataSet.get(i);
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
     * @param i a int.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a double.
     */
    public double getDispersion(int i, ParamSet pars) {
        double res = model.dispersion(dataSet.get(i), pars);
        if (res > 0) {
            return res;
        } else {
            throw new RuntimeException("The returned weight of a data point is infinite. Can not proceed because of infinite point significance.");
        }
    }

    /**
     * Учитывается вероятность, заданная в модели и априорная вероятность
     *
     * @param set a {@link hep.dataforge.datafitter.ParamSet} object.
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
            res += Math.log(getPrior().value(set));
        }
        return res;
    }

    /**
     * Учитывается вероятность, заданная в модели и априорная вероятность
     *
     * @param parName a {@link java.lang.String} object.
     * @param set a {@link hep.dataforge.datafitter.ParamSet} object.
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
            return res += getPrior().derivValue(parName, set) / getPrior().value(set);
        }
        return res;
    }

    /**
     * <p>
     * providesValidAnalyticalDerivs.</p>
     *
     * @param set a {@link hep.dataforge.datafitter.ParamSet} object.
     * @param names a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean providesValidAnalyticalDerivs(ParamSet set, String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException();
        }
        NamedFunction like = this.getLogLike();
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

    /**
     * <p>
     * getLogLike.</p>
     *
     * @return a {@link hep.dataforge.likelihood.LogLikelihood} object.
     */
    public LogLikelihood getLogLike() {
        return new LogLikelihood(this);
    }

    /**
     * <p>
     * Getter for the field <code>prior</code>.</p>
     *
     * @return the prior
     */
    public NamedFunction getPrior() {
        return prior;
    }

    /**
     * <p>
     * Getter for the field <code>model</code>.</p>
     *
     * @return
     */
    public Model getModel() {
        return model;
    }

    /**
     * <p>
     * getModelDim.</p>
     *
     * @return a int.
     */
    public int getModelDim() {
        return model.getDimension();
    }

    /**
     * <p>
     * getModelName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getModelName() {
        return this.model.getName();
    }

    /**
     * <p>
     * Getter for the field <code>dataSet</code>.</p>
     *
     * @return a {@link hep.dataforge.data.DataSet} object.
     */
    public DataSet getDataSet() {
        return dataSet;
    }

    /**
     * <p>
     * getDataNum.</p>
     *
     * @return a int.
     */
    public int getDataSize() {
        return dataSet.size();
    }
}
