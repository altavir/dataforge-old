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
import hep.dataforge.meta.Meta;
import hep.dataforge.stat.parametric.ParametricFunction;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.values.Values;

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

    public static final String WEIGHT = "@weight";

    private final ParametricFunction source;


    public XYModel(ParametricFunction source) {
        super(source, XYAdapter.DEFAULT_ADAPTER);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public double disDeriv(String parName, Values point, Values pars) throws NotDefinedException {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public double dispersion(Values point, Values pars) {
        return 1d / getWeight(point);
    }

    private double getWeight(Values point) {
        if (point.names().contains(WEIGHT)) {
            return point.getDouble(WEIGHT);
        } else {
            double r = adapter.getYerr(point).doubleValue();
            return 1d / (r * r);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double distance(Values point, Values pars) {
        double x = adapter.getX(point).doubleValue();
        double y = adapter.getY(point).doubleValue();
        return value(x, pars) - y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLogProb(Values point, Values pars) {
        double dist = this.distance(point, pars);
        double disp = this.dispersion(point, pars);
        double base; // нормировка
        double xerr = adapter.getXerr(point).doubleValue();
        if (xerr > 0) {
            base = log(2d * Math.PI * sqrt(disp) * xerr);
        } else {
            base = -log(2d * Math.PI * disp) / 2d;
        }
        return -dist * dist / 2d / disp + base;// Внимание! Тут не хи-квадрат, а логарифм правдоподобия
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLogProbDeriv(String parName, Values point, Values pars) {
        return -this.distance(point, pars) * this.disDeriv(parName, point, pars) / this.dispersion(point, pars);
    }

    /**
     * <p>
     * getSpectrum.</p>
     *
     * @return a {@link hep.dataforge.stat.parametric.ParametricFunction} object.
     */
    public ParametricFunction getSpectrum() {
        return this.source;
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
        return providesDeriv(name);
    }

    /**
     * {@inheritDoc}
     *
     * @param x   a double.
     * @param set
     * @return a double.
     */
    public double value(double x, Values set) {
        return source.value(x, set);
    }

    /**
     * <p>derivValue.</p>
     *
     * @param parName a {@link java.lang.String} object.
     * @param x       a double.
     * @param set
     * @return a double.
     */
    public double derivValue(String parName, double x, Values set) {
        return source.derivValue(parName, x, set);
    }
}
