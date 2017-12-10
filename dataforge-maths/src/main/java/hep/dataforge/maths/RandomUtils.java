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

import hep.dataforge.context.Global;
import hep.dataforge.values.Value;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * <p>RandomUtils class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class RandomUtils {
    private static final RandomGenerator defaultGenerator = new JDKRandomGenerator();
    
    /**
     * <p>getDefaultRandomGenerator.</p>
     *
     * @return a {@link org.apache.commons.math3.random.RandomGenerator} object.
     */
    public static RandomGenerator getDefaultRandomGenerator() {
        defaultGenerator.setSeed(Global.instance().getInt("random.seed",-1));
        return defaultGenerator;
    }

    /**
     * <p>setSeed.</p>
     *
     * @param seed a int.
     */
    public static void setSeed(int seed) {
        Global.instance().setValue("random.seed", Value.of(seed));
        defaultGenerator.setSeed(seed);
    }    
}
