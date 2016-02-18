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
package hep.dataforge.dependencies;

import hep.dataforge.content.Named;
import hep.dataforge.meta.Annotated;

/**
 * A uniform named set of dependencies.
 *
 * @author darksnake
 * @param <T>
 */
public interface DependencySet<T> extends Named, Annotated, Iterable<Dependency<T>> {

    /**
     * Get a dependency with given name
     * <p> PENDING support chainpath?</p>
     * @param path
     * @return
     */
    Dependency<T> get(String path);

    /**
     * The actual main type of dependencies in this set. Secondary types are not
     * relevant. The type could be null in this case types of dependencies cold
     * be checked only dynamically.
     *
     * @return
     */
    Class<T> type();

    /**
     * Check if set is empty. Empty set could still have non-empty annotation
     *
     * @return
     */
    default boolean isEmpty(){
        return size() == 0;
    }
    
    int size();
}
