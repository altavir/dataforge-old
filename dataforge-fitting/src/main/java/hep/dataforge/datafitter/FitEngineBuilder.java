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
package hep.dataforge.datafitter;

import hep.dataforge.exceptions.NameNotFoundException;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Alexander Nozik
 */
class FitEngineBuilder {

    private final static HashMap<String, FitEngine> engineList = new HashMap<>();

    /**
     * <p>
     * buildEngine.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.FitEngine} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public static FitEngine buildEngine(String name) throws NameNotFoundException {
        if (engineList.containsKey(name)) {
            return engineList.get(name);
        } else {
            throw new NameNotFoundException(name);
        }
    }

    /**
     * <p>
     * getEngineNameList.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public static Set<String> getEngineNameList() {
        return engineList.keySet();
    }

    /**
     * <p>
     * addEngine.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param ef a {@link java.util.function.Function} object.
     */
    public static void addEngine(String name, FitEngine ef) {
        engineList.put(name.toUpperCase(), ef);
    }

}
