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
package hep.dataforge.stat.parametric;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * <p>FunctionUtils class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class FunctionUtils {

    /**
     * Создает одномерное сечение многомерной именованной функции
     *
     * @param nFunc - исходная именованная функция
     * @param parName - имя параметра, по которому делается сечение
     * @param pars - Точка, вкоторой вычеслено сечение
     * @return a {@link hep.dataforge.stat.parametric.Function} object.
     */
    public static Function getNamedProjection(final ParametricValue nFunc, final String parName, final NamedValueSet pars) {
        return new Function() {
            NamedVector curPars = new NamedVector(pars);

            @Override
            public double derivValue(double x) throws NotDefinedException {
                curPars.setValue(parName, x);
                return nFunc.derivValue(parName, curPars);
            }

            @Override
            public boolean providesDeriv() {
                return nFunc.providesDeriv(parName);
            }

            @Override
            public double value(double x) {
                curPars.setValue(parName, x);
                return nFunc.value(curPars);
            }
        };
    }

    /**
     * <p>getNamedProjectionDerivative.</p>
     *
     * @param nFunc a {@link hep.dataforge.stat.parametric.ParametricValue} object.
     * @param parName a {@link java.lang.String} object.
     * @param derivativeName a {@link java.lang.String} object.
     * @param pars
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     */
    public static UnivariateFunction getNamedProjectionDerivative(final ParametricValue nFunc, 
            final String parName, final String derivativeName, final NamedValueSet pars) {
        return new UnivariateFunction() {
            NamedVector curPars = new NamedVector(pars);

            @Override
            public double value(double x) {
                curPars.setValue(parName, x);
                return nFunc.derivValue(derivativeName, curPars);
            }
        };
    }

    /**
     * <p>getNamedProjectionFunction.</p>
     *
     * @param nFunc a {@link hep.dataforge.stat.parametric.ParametricValue} object.
     * @param parName a {@link java.lang.String} object.
     * @param pars
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     */
    public static UnivariateFunction getNamedProjectionFunction(final ParametricValue nFunc, final String parName, final NamedValueSet pars) {
        return new UnivariateFunction() {
            NamedVector curPars = new NamedVector(pars);

            @Override
            public double value(double x) {
                curPars.setValue(parName, x);
                return nFunc.value(curPars);
            }
        };
    }

    /**
     * Функция, которая запоминает исходную точку, и при нехватке параметров
     * берет значения оттуда.
     *
     * @param func a {@link hep.dataforge.stat.parametric.ParametricValue} object.
     * @param initPars
     * @param freePars - Описывает, каким параметрам можно будет изменяться.
     * Если null, то разрешено изменение всех параметров.
     * @return a {@link hep.dataforge.stat.parametric.ParametricValue} object.
     */
    public static ParametricValue getNamedSubFunction(final ParametricValue func, final NamedValueSet initPars, String... freePars) {
        if (!initPars.names().contains(func.namesAsArray())) {
            throw new IllegalArgumentException("InitPars does not cover all of func parameters.");
        }
        Names names;
        if (freePars.length > 0) {
            names = Names.of(freePars);
        } else {
            names = initPars.names();
        }
        return new AbstractParametricValue(names) {
            private final NamedVector allPars = new NamedVector(initPars);
            private final ParametricValue f = func;

            @Override
            public double derivValue(String derivParName, NamedValueSet pars) {
                if (!pars.names().contains(this.namesAsArray())) {
                    throw new NameNotFoundException();
                }
                for (String name : this.names()) {
                    this.allPars.setValue(name, pars.getDouble(name));
                }
                return f.derivValue(derivParName, allPars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return f.providesDeriv(name) && this.names().contains(name);
            }

            @Override
            public double value(NamedValueSet pars) {
                if (!pars.names().contains(this.namesAsArray())) {
                    throw new NameNotFoundException();
                }
                for (String name : this.names()) {
                    this.allPars.setValue(name, pars.getDouble(name));
                }
                return f.value(allPars);
            }
        };
    }

    /**
     * <p>getSpectrumDerivativeFunction.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param s a {@link hep.dataforge.stat.parametric.ParametricFunction} object.
     * @param pars
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     */
    public static UnivariateFunction getSpectrumDerivativeFunction(final String name, final ParametricFunction s, final NamedValueSet pars) {
        return (double x) -> s.derivValue(name, x, pars);
    }

    /**
     * <p>getSpectrumFunction.</p>
     *
     * @param s a {@link hep.dataforge.stat.parametric.ParametricFunction} object.
     * @param pars
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     */
    public static UnivariateFunction getSpectrumFunction(final ParametricFunction s, final NamedValueSet pars) {
        return (double x) -> s.value(x, pars);
    }

    /**
     * <p>getSpectrumPointFunction.</p>
     *
     * @param s a {@link hep.dataforge.stat.parametric.ParametricFunction} object.
     * @param x a double.
     * @return a {@link hep.dataforge.stat.parametric.ParametricValue} object.
     */
    public static ParametricValue getSpectrumPointFunction(final ParametricFunction s, final double x) {
        return new AbstractParametricValue(s) {

            @Override
            public double derivValue(String derivParName, NamedValueSet pars) throws NotDefinedException, NamingException {
                return s.derivValue(derivParName, x, pars);
            }

            @Override
            public boolean providesDeriv(String name) {
                return s.providesDeriv(name);
            }

            @Override
            public double value(NamedValueSet pars) throws NamingException {
                return s.value(x, pars);
            }
        };
    }
    

}
