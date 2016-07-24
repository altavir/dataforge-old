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
package hep.dataforge.fitting.models;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.fitting.parametric.ParametricFunction;
import hep.dataforge.meta.Meta;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.NamedValueSet;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;

/**
 * Модель для спектра, в качестве ординаты использующего скорость счета Во
 * входных данных может быть указана или скорость счета, или количество отсчетов
 * и время. В первом случае явно должна быть указан вес точки (квадрат ошибки.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class XYModel extends AbstractModel<XYAdapter> {

    private final ParametricFunction source;


    public XYModel(ParametricFunction source) {
        super(source, new XYAdapter());
        this.source = source;
    }


    public XYModel(ParametricFunction source, XYAdapter format) {
        super(source, format);
        this.source = source;
    }


    public XYModel(ParametricFunction source, Meta annotation) {
        super(source, new XYAdapter(annotation));
        this.source = source;
    }


    public XYModel(ParametricFunction source, String xName, String xErrName, String yName, String yErrName) {
        super(source, new XYAdapter(xName, yName, xErrName, yErrName));
        this.source = source;
    }

    public XYModel(ParametricFunction source, String xName, String yName, String yErrName) {
        super(source, new XYAdapter(xName, yName, yErrName));
        this.source = source;
    }

//    public XYModel(String name, NamedSpectrum source, String xName, String yName) {
//        super(name, source, new XYAdapter(xName, yName));
//        this.source = source;
//    }
    /** {@inheritDoc} */
    @Override
    public double disDeriv(String parName, DataPoint point, NamedValueSet pars) throws NotDefinedException {
        if (source.providesDeriv(parName)) {
            if (source.providesDeriv(parName)) {
                return derivValue(parName, adapter.getX(point).doubleValue(), pars);
            } else {
                throw new NotDefinedException();
            }
        } else {
            throw new NotDefinedException(String.format("The derivative for parameter '%s' is not provided by model", parName));
        }
    }

    /** {@inheritDoc} */
    @Override
    public double dispersion(DataPoint point, NamedValueSet pars) {
        return 1 / adapter.getWeight(point);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(DataPoint point, NamedValueSet pars) {
        double x = adapter.getX(point).doubleValue();
        double y = adapter.getY(point).doubleValue();
        return value(x, pars) - y;
    }

    /** {@inheritDoc} */
    @Override
    public double getLogProb(DataPoint point, NamedValueSet pars) {
        double dist = this.distance(point, pars);
        double disp = this.dispersion(point, pars);
        double base; // нормировка
        double xerr = adapter.getXerr(point).doubleValue();
        if (xerr > 0) {
            base = log(2 * Math.PI * sqrt(disp) * xerr);
        } else {
            base = -log(2 * Math.PI * disp) / 2;
        }
        return -dist * dist / 2 / disp + base;// Внимание! Тут не хи-квадрат, а логарифм правдоподобия
    }

    /** {@inheritDoc} */
    @Override
    public double getLogProbDeriv(String parName, DataPoint point, NamedValueSet pars) {
        return -this.distance(point, pars) * this.disDeriv(parName, point, pars) / this.dispersion(point, pars);
    }

    /**
     * <p>
     * getSpectrum.</p>
     *
     * @return a {@link hep.dataforge.fitting.parametric.ParametricFunction} object.
     */
    public ParametricFunction getSpectrum() {
        return this.source;
    }

    /** {@inheritDoc} */
    @Override
    public boolean providesDeriv(String name) {
        return source.providesDeriv(name);
    }

    /** {@inheritDoc} */
    @Override
    public boolean providesProb() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean providesProbDeriv(String name) {
        return providesDeriv(name);
    }

    /**
     * {@inheritDoc}
     *
     * @param x a double.
     * @param set a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a double.
     */
    public double value(double x, NamedValueSet set) {
        return source.value(x, set);
    }

    /**
     * <p>derivValue.</p>
     *
     * @param parName a {@link java.lang.String} object.
     * @param x a double.
     * @param set a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a double.
     */
    public double derivValue(String parName, double x, NamedValueSet set) {
        return source.derivValue(parName, x, set);
    }
}
