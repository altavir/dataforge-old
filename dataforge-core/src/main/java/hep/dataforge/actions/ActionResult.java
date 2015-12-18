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
package hep.dataforge.actions;

import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.io.log.Logable;

/**
 * the temporal storage for components needed to perform an action. It contains
 * the data itself, log and annotation for the pack
 *
 * @author Alexander Nozik
 * @param <T>
 */
public interface ActionResult<T> extends DependencySet<T> {

    @SuppressWarnings("unchecked")
    public static ActionResult empty() {
        return new Pack(null, null, null, null);
    }

//    Item<Dependency<T>> data();
    Logable log();
}
