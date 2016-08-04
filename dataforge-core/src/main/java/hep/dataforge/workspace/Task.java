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

import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;

/**
 * The main building block of "pull" data flow model.
 *
 * @author Alexander Nozik
 * @param <R>
 */
public interface Task<R> extends Named {

    /**
     * A meta node that is used to add additional dependencies to the task
     * manually
     */
    public static final String GATHER_NODE_NAME = "@gather";

    /**
     * Build a model for this task
     *
     * @param workspace
     * @param taskConfig
     * @return
     */
    TaskModel build(Workspace workspace, Meta taskConfig);
    
    /**
     * Check if the model is valid and is acceptable by the task. Throw exception if not.
     * @param model
     * @return 
     */
    void validate(TaskModel model);

    /**
     * Run given task model. Type check expected to be performed before actual
     * calculation.
     *
     * @param model
     * @return
     */
    DataNode<R> run(TaskModel model);

    /**
     * Equals build + run
     *
     * @param workspace
     * @param taskConfig
     * @return
     */
    default DataNode<R> run(Workspace workspace, Meta taskConfig) {
        return run(build(workspace, taskConfig));
    }
}
