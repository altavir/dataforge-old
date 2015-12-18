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

import hep.dataforge.functions.NamedFunction;
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.names.AbstractNamedSet;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Alexander Nozik
 */
class QOWUtils {

    static RealMatrix covarFExp(FitState source, ParamSet set, Weight weight) {
        return covarFExp(source, set, weight.namesAsArray(), weight);
    }

    /**
     * Теоретическая ковариация весовых функций.
     * 
     * D(\phi)=E(\phi_k(\theta_0) \phi_l(\theta_0))= disDeriv_k * disDeriv_l /sigma^2
     * 
     *
     * @param source
     * @param weight
     * @return
     */
    static RealMatrix covarF(FitState source, Weight weight) {

        int fitDim = weight.names().getDimension();
        double[][] res = new double[fitDim][fitDim];

        int i;
        int k;
        int l;

        double summ;

        for (k = 0; k < fitDim; k++) {
            for (l = k; l < fitDim; l++) {
                summ = 0;
                for (i = 0; i < source.getDataSize(); i++) {
                    summ += weight.getDerivs()[k][i] * weight.getDerivs()[l][i] / weight.getDispersion()[i];
                }
                res[k][l] = summ;
                if (l != k) {
                    res[l][k] = summ;
                }
            }
        }
        return new Array2DRowRealMatrix(res);
    }

    /**
     * Экспериментальная ковариация весов. Формула (22) из
     * http://arxiv.org/abs/physics/0604127
     *
     * @param source
     * @param set
     * @param fitPars
     * @param weight
     * @return
     */
    static RealMatrix covarFExp(FitState source, ParamSet set, String[] fitPars, Weight weight) {

        int fitDim = fitPars.length;
        double[][] res = new double[fitDim][fitDim];
        double[][] eqvalues = new double[source.getDataSize()][fitDim];
        /*
         * Важно! Если не делать предварителього вычисления этих производных, то
         * количество вызывов функции будет dim^2 вместо dim Первый индекс -
         * номер точки, второй - номер переменной, по которой берется производная
         */
        int i;
        int k;
        int l;
        for (l = 0; l < fitDim; l++) {
            for (i = 0; i < source.getDataSize(); i++) {
                eqvalues[i][l] = source.getDis(i, set) * weight.getDerivs()[l][i] / weight.getDispersion()[i];
            }
        }
        double summ;

        for (k = 0; k < fitDim; k++) {
            for (l = 0; l < fitDim; l++) {
                summ = 0;
                for (i = 0; i < source.getDataSize(); i++) {
                    summ += eqvalues[i][l] * eqvalues[i][k];
                }
                res[k][l] = summ;
            }
        }
        return new Array2DRowRealMatrix(res);
    }

    /**
     * Берет производные уравнений по параметрам, указанным в весе
     *
     * @param source
     * @param set
     * @param weight
     * @return
     */
    static RealMatrix getEqDerivValues(FitState source, ParamSet set, Weight weight) {
        return getEqDerivValues(source, set, weight.namesAsArray(), weight);
    }

    static RealMatrix getEqDerivValues(FitState source, Weight weight) {
        return getEqDerivValues(source, weight.namesAsArray(), weight);
    }

    /**
     * производные уравнений для метода Ньютона
     *
     * @param source
     * @param set
     * @param fitPars
     * @param weight
     * @return
     */
    static RealMatrix getEqDerivValues(FitState source, ParamSet set, String[] fitPars, Weight weight) {

        int fitDim = fitPars.length;
        //Возвращает производную k-того Eq по l-тому параметру
        double[][] res = new double[fitDim][fitDim];
        double[][] sderiv = new double[source.getDataSize()][fitDim];
        /*
         * Важно! Если не делать предварителього вычисления этих производных, то
         * количество вызывов функции будет dim^2 вместо dim Первый индекс -
         * номер точки, второй - номер переменной, по которой берется производная
         */
        int i;// номер точки из набора данных
        int k;// номер уравнения
        int l;// номер параметра, по короторому берется производная
        for (l = 0; l < fitDim; l++) {
            for (i = 0; i < source.getDataSize(); i++) {
                sderiv[i][l] = source.getDisDeriv(fitPars[l], i, set);

            }
        }
        double summ;

        for (k = 0; k < fitDim; k++) {
            for (l = 0; l < fitDim; l++) {
                summ = 0;
                for (i = 0; i < source.getDataSize(); i++) {
                    // Тут баг, при нулевой дисперсии скатываемся в сингулярность.!!!
                    assert weight.getDispersion()[i] > 0;
                    summ += sderiv[i][l] * weight.getDerivs()[k][i] / weight.getDispersion()[i];
                }
                res[k][l] = summ;
                //TODO Это правильно. Почему??
                if ((source.getPrior() != null)
                        && source.getPrior().names().contains(fitPars[k])
                        && source.getPrior().names().contains(fitPars[l])) {
                    NamedFunction prior = source.getPrior();
                    Logger.getAnonymousLogger().warning("QOW does not interpret prior probability correctly");
                    double pi = prior.value(set);
                    double deriv1 = prior.derivValue(fitPars[k], set);
                    double deriv2 = prior.derivValue(fitPars[l], set);
                    //считаем априорную вероятность независимой для разных переменных
                    res[k][l] += deriv1 * deriv2 / pi / pi;
                }
            }
        }
        return new Array2DRowRealMatrix(res);
    }

    /**
     * Этот метод считает матрицу производных сразу в тета-0. Сильно экономит
     * вызовы функции
     *
     * @param source
     * @param fitPars
     * @param weight
     * @return
     */
    static RealMatrix getEqDerivValues(FitState source, String[] fitPars, Weight weight) {
        int fitDim = fitPars.length;
        double[][] res = new double[fitDim][fitDim];
        int i;
        int k;
        int l;
        double summ;
        for (k = 0; k < fitDim; k++) {
            for (l = 0; l < fitDim; l++) {
                summ = 0;
                for (i = 0; i < source.getDataSize(); i++) {
                    summ += weight.getDerivs()[l][i] * weight.getDerivs()[k][i] / weight.getDispersion()[i];
                }
                res[k][l] = summ;

                //TODO Это правильно. Почему??
                if ((source.getPrior() != null)
                        && source.getPrior().names().contains(fitPars[k])
                        && source.getPrior().names().contains(fitPars[l])) {
                    Logger.getAnonymousLogger().warning("QOW does not interpret prior probability correctly");
                    NamedFunction prior = source.getPrior();
                    double pi = prior.value(weight.getTheta());
                    double deriv1 = prior.derivValue(fitPars[k], weight.getTheta());
                    double deriv2 = prior.derivValue(fitPars[l], weight.getTheta());
                    //считаем априорную вероятность независимой для разный переменных
                    res[k][l] += deriv1 * deriv2 / pi / pi;
                }
            }
        }
        return new Array2DRowRealMatrix(res);
    }

    static NamedDoubleArray getEqValues(FitState source, ParamSet set, Weight weight) {
        return getEqValues(source, set, weight.namesAsArray(), weight);
    }

    /**
     * Значения уравнений метода квазиоптимальных весов
     *
     * @param source
     * @param set
     * @param fitPars
     * @param weight
     * @return
     */
    static NamedDoubleArray getEqValues(FitState source, ParamSet set, String[] fitPars, Weight weight) {

        double[] res = new double[fitPars.length];
        int i;
        int k;
        double summ;

        double[] diss = new double[source.getDataSize()];

        for (i = 0; i < diss.length; i++) {
            diss[i] = source.getDis(i, set);

        }

        for (k = 0; k < fitPars.length; k++) {
            summ = 0;
            for (i = 0; i < source.getDataSize(); i++) {
                summ += diss[i] * weight.getDerivs()[k][i] / weight.getDispersion()[i];
            }
            res[k] = summ;
            //Поправка на априорную вероятность
            if ((source.getPrior() != null) && source.getPrior().names().contains(fitPars[k])) {
                Logger.getAnonymousLogger().warning("QOW does not interpret prior probability correctly");
                NamedFunction prior = source.getPrior();
                res[k] -= prior.derivValue(fitPars[k], set) / prior.value(set);
            }
        }
        return new NamedDoubleArray(fitPars, res);
    }
}

class Weight extends AbstractNamedSet {

    /**
     * Производная спектра по параметру в тета-0 Первый индекс - номер
     * переменной, второй - номер точки из data;
     *
     */
    private double[][] derivs;
    /**
     * КВАДРАТ! ошибки в i-той точке Пока используем экспериментальную ошибку, а
     * там подумаем
     */
    private double[] dispersion;
    private FitState source;
    private NamedDoubleSet theta; // точка, в которой веса вычислены

    Weight(FitState source, String[] list) {
        super(list);
        this.source = source;
    }

    Weight(FitState source, String[] fitPars, ParamSet theta) {
        this(source, fitPars);
        this.update(theta);
    }

    /**
     * Производные от значения спектра в точке по параметрам. Первый индекс -
     * номер точки, второй - номер параметра.
     *
     * @return the derivs
     */
    public double[][] getDerivs() {
        return derivs;
    }

    /**
     * Квадрат ошибки точки.
     *
     * @return the dispersion
     */
    public double[] getDispersion() {
        return dispersion;
    }

    /**
     * Состояние,для которого посчитан вес.
     *
     * @return the source
     */
    public FitState getSource() {
        return source;
    }

    /**
     * Набор параметров, в котором посчитан вес.
     *
     * @return the theta
     */
    public NamedDoubleSet getTheta() {
        if (this.theta == null) {
            throw new IllegalStateException("Update operation for weight is required.");
        }
        return theta;
    }

    /**
     * Обновление весов. На всякий случай требуем явной передачи набора
     * параметров
     * @param set
     */
    final void update(ParamSet set) {

        if (getSource().getDataSize() <= 0) {
            throw new IllegalStateException("Data is not set.");
        }
        theta = set.copy();

        int i;
        int k;
        dispersion = new double[getSource().getDataSize()];
        derivs = new double[getDimension()][getSource().getDataSize()];

        for (i = 0; i < getSource().getDataSize(); i++) {

            this.dispersion[i] = getSource().getDispersion(i, set);
            for (k = 0; k < this.getDimension(); k++) {
                derivs[k][i] = getSource().getDisDeriv(this.names().getName(k), i, set);
            }
        }

    }
}
