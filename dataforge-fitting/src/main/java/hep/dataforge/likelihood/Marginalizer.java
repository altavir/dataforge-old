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
package hep.dataforge.likelihood;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.maths.NamedMatrix;
import static hep.dataforge.maths.RandomUtils.getDefaultRandomGenerator;
import hep.dataforge.maths.integration.DistributionSampler;
import hep.dataforge.maths.integration.Integrand;
import hep.dataforge.maths.integration.MonteCarloIntegrator;
import hep.dataforge.maths.integration.Sampler;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.NamedUtils;
import hep.dataforge.names.Names;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.LoggerFactory;

/**
 * TODO передедать конструкторы
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Marginalizer implements NameSetContainer {

    private static int DEFAULT_MAXIMUM_CALLS = 10000;

    private NamedMatrix cov;
    private RandomGenerator generator;
    private ScaleableNamedFunction like;
    private NamedDoubleArray point;
    private final Names names;

    /**
     * <p>
     * Constructor for Marginalizer.</p>
     *
     * @param cov a {@link hep.dataforge.maths.NamedMatrix} object.
     * @param like a {@link hep.dataforge.likelihood.ScaleableNamedFunction}
     * object.
     * @param point a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @param generator a
     * {@link org.apache.commons.math3.random.RandomGenerator} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public Marginalizer(NamedMatrix cov, ScaleableNamedFunction like, NamedDoubleSet point,
            RandomGenerator generator) throws NameNotFoundException {
        this.names = like.names();
        //считаем, что набор параметров определяется функцией
        this.cov = cov;
        this.like = like;
        //Перенормируем правдоподобие, чтобы не было слишком больших значений
        like.reScale(point);
        this.generator = generator;
        if (!point.names().contains(like.namesAsArray())) {
            throw new NameNotFoundException();
        }
        this.point = new NamedDoubleArray(point);
    }

    /**
     * <p>
     * Constructor for Marginalizer.</p>
     *
     * @param cov a {@link hep.dataforge.maths.NamedMatrix} object.
     * @param like a {@link hep.dataforge.likelihood.ScaleableNamedFunction}
     * object.
     * @param point a {@link hep.dataforge.maths.NamedDoubleSet} object.
     */
    public Marginalizer(NamedMatrix cov, ScaleableNamedFunction like, NamedDoubleSet point) {
        this(cov, like, point, getDefaultRandomGenerator());
    }

    /**
     * <p>
     * getMarginalValue.</p>
     *
     * @param maxCalls a int.
     * @param freePars a {@link java.lang.String} object.
     * @return a double.
     */
    public double getMarginalValue(int maxCalls, final String... freePars) {
        assert (like.names().contains(freePars));
        if (!cov.names().contains(freePars)) {
            throw new NameNotFoundException();
        }
        double[] vals = point.getValues(freePars);
        double[][] mat = cov.getNamedSubMatrix(freePars).getMatrix().getData();

        Sampler sampler = new DistributionSampler(generator, vals, mat);
        MonteCarloIntegrator integrator = new MonteCarloIntegrator();
        MultivariateFunction expLike = new ExpLikelihood(freePars);
        /*
        * Используется нормировка, в которой максимум функции правдопадобия - единица
        * при желании потом можно перенормировать обратно
         */
        Integrand res = integrator.evaluate(expLike, sampler, maxCalls);///exp(like.getScale());
        LoggerFactory.getLogger(getClass()).info("Marginalization complete with {} calls and relative accuracy {}",
                res.getEvaluations(), res.getRelativeAccuracy());
        return res.getValue();
    }

    /**
     * <p>
     * getNorm.</p>
     *
     * @param maxCalls a int.
     * @return a double.
     */
    public double getNorm(int maxCalls) {
        return this.getMarginalValue(maxCalls, like.namesAsArray());
    }

    /**
     * <p>
     * getUnivariateMarginalFunction.</p>
     *
     * @param maxCalls a int.
     * @param parName a {@link java.lang.String} object.
     * @param freePars a {@link java.lang.String} object.
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction}
     * object.
     */
    public UnivariateFunction getUnivariateMarginalFunction(
            final int maxCalls, final String parName, String... freePars) {

        if (!this.names().contains(parName)) {
            throw new NameNotFoundException(parName);
        }

        if (!this.names().contains(freePars)) {
            throw new NameNotFoundException();
        }
        final String[] variablePars;
        if (freePars.length > 0) {
            variablePars = freePars;
        } else {
            variablePars = NamedUtils.exclude(like.names(), parName);
        }

        return (double x) -> {
            point.setValue(parName, x);
            return getMarginalValue(maxCalls, variablePars);
        };

    }

    /**
     * <p>
     * getUnivariateMarginalFunction.</p>
     *
     * @param parName a {@link java.lang.String} object.
     * @param freePars a {@link java.lang.String} object.
     * @return a {@link org.apache.commons.math3.analysis.UnivariateFunction}
     * object.
     */
    public UnivariateFunction getUnivariateMarginalFunction(
            final String parName, String... freePars) {
        return this.getUnivariateMarginalFunction(DEFAULT_MAXIMUM_CALLS, parName, freePars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Names names() {
        return names;
    }

    private class ExpLikelihood implements MultivariateFunction {

        NamedDoubleArray startingPoint;
        String[] freePars;

        ExpLikelihood(String[] freePars) {
            this.startingPoint = new NamedDoubleArray(point);
            this.freePars = freePars;
        }

        @Override
        public double value(double[] point) {
            for (int i = 0; i < freePars.length; i++) {
                startingPoint.setValue(freePars[i], point[i]);
            }
            return like.expValue(startingPoint);
        }
    }
}
