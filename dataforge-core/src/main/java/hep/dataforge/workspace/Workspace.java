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
import hep.dataforge.data.*;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.GenericBuilder;
import org.jetbrains.annotations.Nullable;

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
    default Data<?> getData(String dataPath) {
        return getData().optData(dataPath).get();
    }

    /**
     * Get the whole data tree
     *l
     * @return
     */
    DataNode<Object> getData();

    /**
     * Get task by name. Throw {@link hep.dataforge.exceptions.NameNotFoundException} if task with given name does not exist.
     *
     * @param taskName
     * @return
     */
    Task<?> getTask(String taskName);

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
     * @param overlay  use given meta as overaly for existing meta with the same name
     * @return
     */
    default DataNode<?> runTask(String taskName, Meta config, boolean overlay) {
        Task<?> task = getTask(taskName);
        if (overlay && hasTarget(config.getName())) {
            config = new Laminate(config, getTarget(config.getName()));
        }
        TaskModel model = task.build(this, config);
        return runTask(model);
    }

    default DataNode<?> runTask(String taskName, Meta config) {
        return this.runTask(taskName, config, true);
    }

    /**
     * Use config root node name as task name
     *
     * @param config
     * @return
     */
    default DataNode<?> runTask(Meta config) {
        return runTask(config.getName(), config);
    }

    /**
     * Run task using meta previously stored in workspace.
     *
     * @param taskName
     * @param target
     * @return
     */
    default DataNode<?> runTask(String taskName, String target) {
        return runTask(taskName, getTarget(target));
    }

    /**
     * Run task for target with the same name as task name
     *
     * @param taskName
     * @return
     */
    default DataNode<?> runTask(String taskName) {
        return runTask(taskName, taskName);
    }

    /**
     * Run task with given model.
     *
     * @param model
     * @return
     */
    default DataNode<?> runTask(TaskModel model) {
        return this.getTask(model.getName()).run(model);
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
     *
     * @param name
     * @return
     */
    boolean hasTarget(String name);

    /**
     * Get stream of meta objects stored in the Workspace. Not every target is valid for every task.
     *
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
                    target(configMeta.getString("name"), configMeta.getMeta("meta"));
                });
            }

            return self();
        }

        B setContext(Context ctx);

        B loadData(String as, Data<?> data);

        /**
         * Load a data node to workspace data tree.
         *
         * @param as       path to the new node in the data tree could be empty
         * @param datanode
         * @return
         */
        B loadData(@Nullable String as, DataNode<?> datanode);

        /**
         * Load a data node generated by given DataLoader
         *
         * @param place
         * @param factory
         * @param dataConfig
         * @return
         */
        default B loadData(String place, DataLoader<?> factory, Meta dataConfig) {
            return loadData(place, factory.build(getContext(), dataConfig));
        }

        /**
         * Load data using data factory serviceLoader
         *
         * @param place
         * @param factoryType
         * @param dataConfig
         * @return
         */
        default B loadData(String place, String factoryType, Meta dataConfig) {
            return loadData(place, DataLoader.getFactory(factoryType), dataConfig);
        }

        default B loadFileData(String place, String filePath, Meta meta) {
            return loadData(place, FileDataFactory.buildFileData(getContext(), filePath, meta));
        }

        default B loadFileData(String dataName, String filePath) {
            return loadFileData(dataName, filePath, Meta.empty());
        }

        B target(String name, Meta meta);

        default B target(Meta meta) {
            return target(meta.getName(), meta);
        }

        B loadTask(Task task);

        default B loadTask(Class<Task> type) throws IllegalAccessException, InstantiationException {
            return loadTask(type.newInstance());
        }
    }

}
