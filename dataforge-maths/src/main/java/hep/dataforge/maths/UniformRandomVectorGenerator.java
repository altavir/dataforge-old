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

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomVectorGenerator;

import static hep.dataforge.maths.RandomUtils.getDefaultRandomGenerator;

/**
 * <p>UniformRandomVectorGenerator class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class UniformRandomVectorGenerator implements RandomVectorGenerator {
    private static final int MAX_TRIES_PER_CALL = 20;
    private final Domain domain;
    private final RandomGenerator generator;

    public UniformRandomVectorGenerator(RandomGenerator generator, Domain domain) {
        this.generator = generator;
        this.domain = domain;
    }

    public UniformRandomVectorGenerator(Domain domain) {
        this.generator = INSTANCE.getDefaultRandomGenerator();
        this.domain = domain;
    }

    private double[] next() {
        double[] res = new double[domain.getDimension()];
        double a, b;
        for (int i = 0; i < res.length; i++) {
            a = domain.getLowerBound(i);
            b = domain.getUpperBound(i);
            assert b >= a;
            res[i] = a + generator.nextDouble() * (b - a);

        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] nextVector() {
        double[] res = this.next();
        int i = 0;
        while (!domain.contains(res)) {
            res = this.next();
            i++;
            if (i >= MAX_TRIES_PER_CALL) {
                throw new TooManyEvaluationsException(MAX_TRIES_PER_CALL);
            }
        }
        return res;
    }

}
