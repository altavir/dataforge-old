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
package hep.dataforge.MINUIT;

/**
 *
 * @version $Id$
 */
class ScanMinimizer extends ModularFunctionMinimizer {
    private ScanBuilder theBuilder;
    private SimplexSeedGenerator theSeedGenerator;

    ScanMinimizer() {
        theSeedGenerator = new SimplexSeedGenerator();
        theBuilder = new ScanBuilder();
    }

    @Override
            MinimumBuilder builder() {
        return theBuilder;
    }

    @Override
            MinimumSeedGenerator seedGenerator() {
        return theSeedGenerator;
    }
}
