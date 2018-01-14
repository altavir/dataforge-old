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
package hep.dataforge.workspace.tasks;

import hep.dataforge.data.DataNode;
import hep.dataforge.description.Described;
import hep.dataforge.io.markup.MarkupBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.workspace.Workspace;

/**
 * The main building block of "pull" data flow model.
 *
 * @param <R>
 * @author Alexander Nozik
 */
public interface Task<R> extends Named, Described {
    String TASK_TARGET = "task";

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
     *
     * @param model
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
     * Equals builder + run
     *
     * @param workspace
     * @param taskConfig
     * @return
     */
    default DataNode<R> run(Workspace workspace, Meta taskConfig) {
        return run(build(workspace, taskConfig));
    }

//    @Override
//    default NodeDescriptor getDescriptor() {
//        return Descriptors.buildDescriptor(getName(), getClass());
//    }

    @Override
    default MarkupBuilder getHeader() {
        return new MarkupBuilder().text(getName(), "blue");
    }

    /**
     * If true, the task is designated as terminal.
     * Terminal task is executed immediately after {@code run} is called, without any lazy calculations.
     * @return
     */
    default boolean isTerminal(){
        return false;
    }
}
