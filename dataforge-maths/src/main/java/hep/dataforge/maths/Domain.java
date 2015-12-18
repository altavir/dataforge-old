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
package hep.dataforge.maths;

import hep.dataforge.exceptions.NotDefinedException;
import org.apache.commons.math3.linear.RealVector;

/**
 * Класс описывающий замкнутую поверхность в н-мерии
 * FIXME заменить на именованый аналог
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Domain {

    /**
     * <p>contains.</p>
     *
     * @param point a {@link org.apache.commons.math3.linear.RealVector} object.
     * @return a boolean.
     */
    boolean contains(RealVector point);

    /**
     * <p>contains.</p>
     *
     * @param point an array of double.
     * @return a boolean.
     */
    boolean contains(double[] point);

    /**
     * Заготовка на будущее для всяческих геометрических заморочек
     *
     * @param point a {@link org.apache.commons.math3.linear.RealVector} object.
     * @return a {@link org.apache.commons.math3.linear.RealVector} object.
     */
    RealVector nearestInDomain(RealVector point);

    /**
     * <p>getLowerBound.</p>
     *
     * @param num a int.
     * @param point a {@link org.apache.commons.math3.linear.RealVector} object.
     * @return a {@link java.lang.Double} object.
     */
    Double getLowerBound(int num, RealVector point);

    /**
     * <p>getUpperBound.</p>
     *
     * @param num a int.
     * @param point a {@link org.apache.commons.math3.linear.RealVector} object.
     * @return a {@link java.lang.Double} object.
     */
    Double getUpperBound(int num, RealVector point);

    /**
     * Абсолютные максимумы и минимумы по координате для всей области.
     *
     * @param num a int.
     * @return a {@link java.lang.Double} object.
     * @throws hep.dataforge.exceptions.NotDefinedException if any.
     */
    Double getLowerBound(int num) throws NotDefinedException;

    /**
     * <p>getUpperBound.</p>
     *
     * @param num a int.
     * @return a {@link java.lang.Double} object.
     * @throws hep.dataforge.exceptions.NotDefinedException if any.
     */
    Double getUpperBound(int num) throws NotDefinedException;

    /**
     * <p>volume.</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    Double volume();

    /**
     * <p>getDimension.</p>
     *
     * @return a int.
     */
    int getDimension();

    /**
     * <p>isFixed.</p>
     *
     * @param num a int.
     * @return a boolean.
     */
    boolean isFixed(int num);
}
