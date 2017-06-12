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

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;

import java.util.stream.Stream;

/**
 * <p>Abstract Sampler class.</p>
 *
 * @author Alexander Nozik
 */
public abstract class Sampler {

    protected final RandomGenerator generator;


    public Sampler(RandomGenerator generator) {
        this.generator = generator;
    }


    public Sampler() {
        generator = new SynchronizedRandomGenerator(new JDKRandomGenerator());
    }


    public abstract Sample nextSample();

    public Stream<Sample> stream() {
        return Stream.generate(this::nextSample);
    }

    public abstract int getDimension();
}
