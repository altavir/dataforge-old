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
package hep.dataforge.maths.integration;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;

/**
 * <p>Abstract Sampler class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class Sampler {

    protected final RandomGenerator generator;

    /**
     * <p>Constructor for Sampler.</p>
     *
     * @param generator a {@link org.apache.commons.math3.random.RandomGenerator} object.
     */
    public Sampler(RandomGenerator generator) {
        this.generator = generator;
    }

    /**
     * <p>Constructor for Sampler.</p>
     */
    public Sampler() {
        generator = new SynchronizedRandomGenerator(new JDKRandomGenerator());
    }

    /**
     * <p>nextSample.</p>
     *
     * @return a {@link hep.dataforge.maths.integration.Sample} object.
     */
    public abstract Sample nextSample();

    /**
     * <p>nextNSamples.</p>
     *
     * @param n a int.
     * @return a {@link java.util.List} object.
     */
    public List<Sample> nextNSamples(int n) {
        List<Sample> res = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            res.add(nextSample());
        }
        return res;
    }
    
    /**
     * <p>getDimension.</p>
     *
     * @return a int.
     */
    public abstract int getDimension();
}
