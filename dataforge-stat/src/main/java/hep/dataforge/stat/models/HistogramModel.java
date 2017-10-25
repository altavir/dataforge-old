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
package hep.dataforge.stat.models;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.maths.integration.GaussRuleIntegrator;
import hep.dataforge.maths.integration.UnivariateIntegrator;
import hep.dataforge.meta.Meta;
import hep.dataforge.stat.parametric.ParametricFunction;
import hep.dataforge.stat.parametric.ParametricUtils;
import hep.dataforge.values.Values;
import org.apache.commons.math3.analysis.UnivariateFunction;

import static java.lang.Math.log;

/**
 * Модель для гистограммы. В общем случае размещение и размер бинов может быть
 * произвольным. ВАЖНО! Для того, чтобы не было смешения, при вычислении
 * количества отсчетов в каждом бине, берется интеграл от спектра. Эта операция
 * может быть очень накладной, поэтому в тех случаях, когда бины очень
 * маленькие, лучше использовать SpectrumModel, где берется точечная оценка.
 * <p>
 * Количества отсчетов не нормируются на ширину бина.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class HistogramModel extends AbstractModel<HistogramAdapter> {

    private final UnivariateIntegrator integrator = new GaussRuleIntegrator(10);
    private final ParametricFunction source;

    private boolean calculateCountInBin = false;

    public HistogramModel(Meta meta, ParametricFunction source) {
        super(meta, source.getNames(), new HistogramAdapter());
        this.source = source;
    }

    public HistogramModel(Meta meta, ParametricFunction source, String binBeginName, String binEndName, String countName) {
        super(meta, source.getNames(), new HistogramAdapter(binBeginName, binEndName, countName));
        this.source = source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double disDeriv(String parName, Values point, Values pars) throws NotDefinedException {
        if (source.providesDeriv(parName)) {
            return this.derivValue(parName, adapter.getBinBegin(point), adapter.getBinEnd(point), pars);
        } else {
            throw new NotDefinedException(String.format("The derivative for parameter '%s' is not provided by model", parName));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double dispersion(Values point, Values pars) {
        double res = this.value(adapter.getBinBegin(point), adapter.getBinEnd(point), pars);
        if (res < 1) {
            return 1;
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double distance(Values point, Values pars) {
//        double x = point.binCenter();
        long y = adapter.getCount(point);
        return this.value(adapter.getBinBegin(point), adapter.getBinEnd(point), pars) - y;
    }

    public boolean isCalculateCountInBin() {
        return calculateCountInBin;
    }

    public void setCalculateCountInBin(boolean calculateCountInBin) {
        this.calculateCountInBin = calculateCountInBin;
    }

    public double value(double binBegin, double binEnd, Values set) {
        if (isCalculateCountInBin()) {
            UnivariateFunction spFunc = ParametricUtils.getSpectrumFunction(source, set);
            return integrator.evaluate(binBegin, binEnd, spFunc).getValue();
        } else {
            return source.value((binBegin + binEnd) / 2, set) * (binEnd - binBegin);
        }

    }

    public double derivValue(String parName, double binBegin, double binEnd, Values set) {
        if (isCalculateCountInBin()) {
            UnivariateFunction spFunc = ParametricUtils.getSpectrumDerivativeFunction(parName, source, set);
            return integrator.evaluate(binBegin, binEnd, spFunc).getValue();
        } else {
            double val = (binBegin + binEnd) / 2;
            return source.derivValue(parName, val, set) * (binEnd - binBegin);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLogProb(Values point, Values pars) {
        double dist = this.distance(point, pars);
        double disp = this.dispersion(point, pars);
        double base = -log(2 * Math.PI * disp) / 2; // нормировка
        return -dist * dist / 2 / disp + base;// Внимание! Тут не хи-квадрат, а логарифм правдоподобия
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLogProbDeriv(String parName, Values point, Values pars) {
        return -this.distance(point, pars) * this.disDeriv(parName, point, pars) / this.dispersion(point, pars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesDeriv(String name) {
        return source.providesDeriv(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesProb() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesProbDeriv(String name) {
        return true;
    }
}
