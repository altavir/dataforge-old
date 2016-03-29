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
import hep.dataforge.meta.Meta;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hep.dataforge.data.DataNode;

/**
 * A generic implementation of task with 4 phases:
 * <ul>
 * <li>gathering</li>
 * <li>transformation</li>
 * <li>reporting</li>
 * <li>result generation</li>
 * </ul>
 *
 * @author Alexander Nozik
 */
public abstract class GenericTask<T> implements Task<T> {

    @Override
    public DataNode<T> run(Workspace workspace, Meta config) {
//        logger.info("Starting task '{}'", getName());
//        List<String> targetList = generateTargets(config, targets);

        getLogger().debug("Creating executor...");
        final TaskExecutor executor = new TaskExecutor(workspace.getWorkspaceThreadGroup(), getName());

        Future<DataNode<T>> res = executor.submit(() -> {
            getLogger().info("Starting gathering phase");

            TaskState state = new TaskState(gather(executor, workspace, config));

            getLogger().info("Starting transformation phase");
            transform(executor, workspace.getContext(), state, config);

            getLogger().info("Starting report phase");
            report(executor, workspace.getContext(), state, config);

            getLogger().info("Starting result generation");
            return result(executor, workspace, state, config);
        });

        try {
            return res.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException("Uncached exception during task execution", ex);
        }
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(getName());
    }

    /**
     * Gathering of dependencies from workspace
     *
     * @param executor
     * @param workspace
     * @param config
     * @return
     */
    protected abstract DataNode gather(TaskExecutor executor, Workspace workspace, Meta config);

    /**
     * The main task body
     *
     * @param executor
     * @param state
     * @param config
     */
    protected abstract void transform(TaskExecutor executor, Context context, TaskState state, Meta config);

    /**
     * Reporting task results. No change of state is allowed.
     *
     * @param executor
     * @param state
     * @param config
     */
    protected abstract void report(TaskExecutor executor, Context context, TaskState state, Meta config);

    /**
     * Generating result and storing it in workspace.
     *
     * @param executor
     * @param workspace
     * @param state
     * @param config
     * @return
     */
    protected abstract DataNode<T> result(TaskExecutor executor, Workspace workspace, TaskState state, Meta config);

}
