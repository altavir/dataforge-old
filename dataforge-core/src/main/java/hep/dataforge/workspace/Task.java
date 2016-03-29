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

import hep.dataforge.names.Named;
import hep.dataforge.meta.Meta;
import hep.dataforge.data.DataNode;

/**
 * The replacement for Action, task is a general way to define any process in a
 * declarative way.
 *
 * @author Alexander Nozik
 * @param <T>
 */
public interface Task<R> extends Named {

//    /**
//     * Run the task with given configuration and list of desired targets and
//     * return a set of named results (names are the target names). If target
//     * list is empty then all targets supported by configuration are calculated.
//     * Only those targets that supported by configuration are calculated even if
//     * they are explicitly mentioned in target list. The resulting dependency
//     * set is not guaranteed to contain all given targets.
//     *
//     * @param config the meta-data of the task
//     * @return
//     */
    
    
    
    DataNode<R> run(Workspace workspace, Meta config);

}
