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
 *
 *
 * @author Alexander Nozik
 * @param <T>
 */
public interface Task<R> extends Named {
    /**
     * A meta node that is used to add additional dependencies to the task manually
     */
    public static final String GATHER_NODE_NAME = "@gather";

    DataNode<R> run(Workspace workspace, TaskModel model);

    TaskModel generateModel(Workspace workspace, Meta modelMeta, Meta... dataModel);

    default DataNode<R> run(Workspace workspace, Meta modelMeta, Meta... dataModel) {
        return run(workspace, generateModel(workspace, modelMeta, dataModel));
    }
}
