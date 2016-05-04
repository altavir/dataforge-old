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

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.context.ProcessManager;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataFactory;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.data.FileDataFactory;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaProvider;
import hep.dataforge.utils.GenericBuilder;

/**
 * A place to store tasks and their results
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Workspace extends Encapsulated, MetaProvider {

    public static final String DATA_STAGE_NAME = "@data";

    /**
     * Get specific static data. Null if no data with given name is found
     *
     * @param dataPath Fully qualified data name
     * @return
     */
    default Data getData(String dataPath) {
        return getDataStage().getData(dataPath);
    }

    /**
     * Get the whole data tree
     *
     * @return
     */
    default DataNode getDataStage() {
        return getStage(DATA_STAGE_NAME);
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
        return this.<T>getTask(taskName).run(this, generateModel(taskName, config));
    }

    default <T> DataNode<T> runTask(TaskModel model) {
        return this.<T>getTask(model.getName()).run(this, model);
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
     * Get a predefined meta with given name
     *
     * @param name
     * @return
     */
    @Override
    Meta getMeta(String name);

    /**
     * Generate a taskModel for given task configuration
     *
     * @param config
     * @return
     */
    TaskModel generateModel(String taskName, Meta taskMeta, Meta dependencies);

    default TaskModel generateModel(String taskName, Meta taskMeta) {
        return generateModel(taskName, taskMeta, taskMeta.getNode("gather", Meta.empty()));
    }

    /**
     * Build an input data node for task based on the taskModel using all
     * necessary checks. Tasks could internally use or override this procedure
     *
     * @param model
     * @return
     */
    DataTree.Builder buildDataNode(ProcessManager.Callback callback, TaskModel model);

    public interface Builder<B extends Builder> extends GenericBuilder<Workspace, B>, Encapsulated {

        default B loadFrom(Meta meta) {
            if (meta.hasValue("context")) {
                setContext(GlobalContext.getContext(meta.getString("context")));
            }
            if (meta.hasNode("data")) {
                meta.getNodes("data").forEach((Meta dataMeta) -> {
                    DataFactory factory;
                    if (dataMeta.hasValue("dataFactoryClass")) {
                        try {
                            factory = (DataFactory) Class.forName(dataMeta.getString("dataFactoryClass")).newInstance();
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                            throw new RuntimeException("Error while initializing data factory", ex);
                        }
                    } else {
                        factory = new FileDataFactory();
                    }
                    String as = dataMeta.getString("as", "");
                    loadData(as, factory.build(getContext(), dataMeta));
                });
            }
            if (meta.hasNode("config")) {
                meta.getNodes("config").forEach((Meta configMeta) -> {
                    loadMeta(configMeta.getString("name"), configMeta.getNode("meta"));
                });
            }

            return self();
        }

        B setContext(Context ctx);

        B loadData(String name, Data data);

        B loadData(String name, DataNode datanode);

        default B loadData(String name, DataFactory factory, Meta dataConfig) {
            return loadData(name, factory.build(getContext(), dataConfig));
        }

        default B putFile(String dataName, String filePath, Meta meta) {
            return loadData(dataName, FileDataFactory.buildFileData(getContext(), filePath, meta));
        }

        default B putFile(String dataName, String filePath) {
            return putFile(dataName, filePath, null);
        }

        B loadMeta(String name, Meta meta);

        B loadTask(Task task);

    }

}
