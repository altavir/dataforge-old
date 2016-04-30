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

import hep.dataforge.context.Encapsulated;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;

/**
 * A place to store tasks and their results
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Workspace extends Annotated, Named, Encapsulated {

    public static final String DATA_STAGE_NAME = "@data";

    /**
     * Get specific static data. Null if no data with given name is found
     *
     * @param dataName Fully qualified data name
     * @return
     */
    default Data getData(String dataName) {
        return getStage(DATA_STAGE_NAME).getData(dataName);
    }

    /**
     * Get specific stage
     *
     * @param <T>
     * @param stageName
     * @return
     */
    <T> DataNode<T> getStage(String stageName);

    <T> Task<T> getTask(String taskName);

    /**
     * Check task dependencies and run it with given configuration or load
     * result from cache if it is available
     *
     * @param taskName
     * @param config
     * @return
     */
    default <T> DataNode<T> runTask(String taskName, Meta config) {
        return this.<T>getTask(taskName).run(this, config);
    }

    /**
     * Update existing or create new stage
     *
     * @param <T>
     * @param data
     * @return
     */
    <T> DataNode<T> updateStage(String stage, DataNode<T> data);

    /**
     * Get the identity for this workspace including context identity
     *
     * @return
     */
    Identity getIdentity();

    /**
     * Get a predefined meta with given name
     *
     * @param name
     * @return
     */
    Meta getMeta(String name);

}
