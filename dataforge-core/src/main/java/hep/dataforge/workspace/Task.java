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

import hep.dataforge.content.Named;
import hep.dataforge.meta.Meta;
import java.util.concurrent.ExecutionException;
import hep.dataforge.dependencies.Data;
import hep.dataforge.dependencies.DataNode;

/**
 * The replacement for Action, task is a general way to define any process in a
 * declarative way.
 *
 * @author Alexander Nozik
 * @param <T>
 */
public interface Task<T> extends Named {

    public static final String DEFAULT_TARGET = "";

    /**
     * Run the task with given configuration and list of desired targets and
     * return a set of named results (names are the target names). If target
     * list is empty then all targets supported by configuration are calculated.
     * Only those targets that supported by configuration are calculated even if
     * they are explicitly mentioned in target list. The resulting dependency
     * set is not guaranteed to contain all given targets.
     *
     * @param config the meta-data of the task
     * @return
     */
    DataNode<T> run(Meta config, String... targets) throws InterruptedException, ExecutionException;

    /**
     * Run task and return result for specific target. Could be optimized to
     * ignore all other targets if overridden.
     *
     * @param config
     * @param target
     * @return
     */
    default Data<T> runForTarget(Meta config, String target) throws InterruptedException, ExecutionException {
        return run(config, target).get(target);
    }

    /**
     * Run task for default target. If task return only one target, than it is
     * returned ignoring its name, otherwise, target with default name is
     * returned.
     *
     * @param config
     * @return
     */
    default Data<T> runForDefaultTarget(Meta config) throws InterruptedException, ExecutionException {
        DataNode<T> map = run(config);
        if (map.size() == 1) {
            return map.iterator().next();
        } else {
            return map.get(DEFAULT_TARGET);
        }
    }

    /**
     * The method to check task identity. Task do not have state, so the only
     * thing that could be changed is the bytecode itself.
     *
     * @return
     */
    default Identity getIdentity() {
        return new ClassIdentity(this.getClass());
    }
}
