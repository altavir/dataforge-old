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
import hep.dataforge.context.Global;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataFactory;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.FileDataFactory;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.GenericBuilder;

import java.util.stream.Stream;

/**
 * A place to store tasks and their results
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Workspace extends Encapsulated {

    String DATA_STAGE_NAME = "@data";

    /**
     * Get specific static data. Null if no data with given name is found
     *
     * @param dataPath Fully qualified data name
     * @return
     */
    default Data getData(String dataPath) {
        return getData().getData(dataPath).get();
    }

    /**
     * Get the whole data tree
     *
     * @return
     */
    DataNode<Object> getData();

    /**
     * Get task by name. Throw {@link hep.dataforge.exceptions.NameNotFoundException} if task with given name does not exist.
     * @param taskName
     * @param <T>
     * @return
     */
    <T> Task<T> getTask(String taskName);

    /**
     * The stream of available tasks
     *
     * @return
     */
    Stream<Task> getTasks();

    /**
     * Check task dependencies and run it with given configuration or load
     * result from cache if it is available
     *
     * @param taskName
     * @param config
     * @param overlay usegiven meta as overaly for existing meta with the same name
     * @return
     */
    default <T> DataNode<T> runTask(String taskName, Meta config, boolean overlay) {
        Task<T> task = getTask(taskName);
        if (overlay && hasTarget(config.getName())) {
            config = new Laminate(config, getTarget(config.getName()));
        }
        TaskModel model = task.build(this, config);
        return runTask(model);
    }

    default <T> DataNode<T> runTask(String taskName, Meta config) {
        return this.runTask(taskName, config, false);
    }

    /**
     * Use config root node name as task name
     *
     * @param config
     * @param <T>
     * @return
     */
    default <T> DataNode<T> runTask(Meta config) {
        return runTask(config.getName(), config);
    }

    /**
     * Run task using meta previously stored in workspace.
     *
     * @param <T>
     * @param taskName
     * @param target
     * @return
     */
    default <T> DataNode<T> runTask(String taskName, String target) {
        return runTask(taskName, getTarget(target));
    }

    /**
     * Run task with given model.
     * @param model
     * @param <T>
     * @return
     */
    default <T> DataNode<T> runTask(TaskModel model) {
        return this.<T>getTask(model.getName()).run(model);
    }

    /**
     * Get a predefined meta with given name
     *
     * @param name
     * @return
     */
    Meta getTarget(String name);

    /**
     * Check if workspace contains given target
     * @param name
     * @return
     */
    boolean hasTarget(String name);

    /**
     * Get stream of meta objects stored in the Workspace. Not every target is valid for every task.
     * @return
     */
    Stream<Meta> getTargets();

    /**
     * Clean up workspace. Invalidate caches etc.
     */
    void clean();

    interface Builder<B extends Builder> extends GenericBuilder<Workspace, B>, Encapsulated {

        default B loadFrom(Meta meta) {
            if (meta.hasValue("context")) {
                setContext(Global.getContext(meta.getString("context")));
            }
            if (meta.hasMeta("data")) {
                meta.getMetaList("data").forEach((Meta dataMeta) -> {
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
            if (meta.hasMeta("config")) {
                meta.getMetaList("config").forEach((Meta configMeta) -> {
                    loadMeta(configMeta.getString("name"), configMeta.getMeta("meta"));
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

        default B loadFile(String dataName, String filePath, Meta meta) {
            return loadData(dataName, FileDataFactory.buildFileData(getContext(), filePath, meta));
        }

        default B loadFile(String dataName, String filePath) {
            return loadFile(dataName, filePath, Meta.empty());
        }

        B loadMeta(String name, Meta meta);

        default B loadMeta(Meta meta) {
            return loadMeta(meta.getName(), meta);
        }

        B loadTask(Task task);

        default B loadTask(Class<Task> type) throws IllegalAccessException, InstantiationException {
            return loadTask(type.newInstance());
        }
    }

}
