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

import hep.dataforge.stat.fit.Param;
import hep.dataforge.stat.fit.ParamSet;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.meta.Annotated;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Named;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.PointSource;
import hep.dataforge.values.NamedValueSet;
import hep.dataforge.stat.parametric.ParametricValue;

/**
 *
 * @author Alexander Nozik
 */
public interface Model extends NameSetContainer, Annotated {

    /**
     * Fit function value minus data point value
     *
     * @param point
     * @param pars
     * @return
     */
    double distance(DataPoint point, NamedValueSet pars);

    /**
     *
     * @param parName
     * @param point
     * @param pars
     * @return
     * @throws NotDefinedException
     */
    double disDeriv(String parName, DataPoint point, NamedValueSet pars) throws NotDefinedException;

    /**
     * get inversed weight of the point
     *
     * @param point
     * @param pars
     * @return
     */
    double dispersion(DataPoint point, NamedValueSet pars);

    /**
     * Provides a ln of probability of obtaining the data point with given
     * parameter set
     *
     * @param point
     * @param pars
     * @return
     */
    double getLogProb(DataPoint point, NamedValueSet pars) throws NotDefinedException;

    /**
     * Модель имеет собственное распределение ошибок
     *
     * @return
     */
    boolean providesProb();

    /**
     *
     * @param parName
     * @param point
     * @param pars
     * @return
     * @throws NotDefinedException
     */
    double getLogProbDeriv(String parName, DataPoint point, NamedValueSet pars) throws NotDefinedException;

    /**
     * Модель возвращает аналитическую производную распределения ошибок
     *
     * @param name
     * @return
     */
    boolean providesProbDeriv(String name);

    /**
     * Модель возвращает аналитическую производную расстояния до точки по
     * данному параметру
     *
     * @param name
     * @return
     */
    boolean providesDeriv(String name);

    /**
     *
     * @param point
     * @return
     */
    public ParametricValue getDistanceFunction(DataPoint point);

    /**
     *
     * @param point
     * @return
     */
    public ParametricValue getLogProbFunction(DataPoint point);

    /**
     * Пытается угадать набор параметров по набору данных. По-умолчанию этот
     * метод не имеет реализации, но может быть
     *
     * @param data
     * @return
     */
    default public ParamSet getParametersGuess(PointSource data) {
        throw new NotDefinedException("Initial guess not defined");
    }

    /**
     * Возвращает значение параметра по-умолчанию
     *
     * @param name
     * @return
     */
    default public Param getDefaultParameter(String name) {
        throw new NotDefinedException("Default parameter not found");
    }

}
