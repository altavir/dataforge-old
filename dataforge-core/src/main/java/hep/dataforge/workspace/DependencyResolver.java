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
package hep.dataforge.workspace;

import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.meta.Meta;

/**
 * Dependency resolver is the tool used to validate for dependency availability,
 * and provide it.
 *
 * @author Alexander Nozik
 */
public interface DependencyResolver {

    /**
     * Provide dependency set for specific task with given configuration. Does
     * not perform validate operations, so it should be performed manually.
     *
     * @param taskName
     * @param meta
     * @throws DependencyResolutionException if there is some problem while
     * resolving direct dependency.
     * @return
     */
    DependencySet resolve(String taskName, Meta meta) throws DependencyResolutionException;

    /**
     * Check the whole dependency tree searching for:
     * <ul>
     * <li>Missed dependencies</li>
     * <li>Main dependency type mismatch (secondary types by default are not
     * checked)</li>
     * <li>Dependency graph cycles</li>
     * </ul>
     * If caching is used, than all invalid cache entries are invalidated
     * automatically. It also generates appropriate log entries for warnings.
     *
     * @param taskName
     * @param meta
     * @throws DependencyResolutionException if some problem with dependency
     * graph is found PENDING replace exception by returned object, say
     * DependencyConflict
     * @return true if everything is OK
     */
    boolean validate(String taskName, Meta meta) throws DependencyResolutionException;
}
