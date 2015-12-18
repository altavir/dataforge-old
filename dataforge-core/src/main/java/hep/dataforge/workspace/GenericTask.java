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
import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.meta.Meta;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final String name;
    private final Workspace workspace;
    protected Logger logger;
    protected TaskProgressListener listener;

    public GenericTask(String name, Workspace workspace) {
        this.name = name;
        this.workspace = workspace;
        logger = LoggerFactory.getLogger(name);
    }

    @Override
    public DependencySet<T> run(Meta config, String... targets) throws InterruptedException, ExecutionException {
        logger.info("Starting task '{}'", getName());
        List<String> targetList = generateTargets(config, targets);

        logger.debug("Creating executor...");
        final TaskExecutor executor = new TaskExecutor(workspace.getWorkspaceThreadGroup(), name, listener);

        Future<DependencySet<T>> res = executor.submit(() -> {
            logger.info("Starting gathering phase");
            
            TaskState state = new TaskState(gather(executor, getWorkspace(), config, targetList), targetList);

            logger.info("Starting transformation phase");
            transform(executor, state, config);

            logger.info("Starting report phase");
            report(executor, getWorkspace().getContext(), state, config);

            logger.info("Starting result generation");
            return result(executor, getWorkspace(), state, config);
        });

        return res.get();
    }

    public void setListener(TaskProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public String getName() {
        return name;
    }

    protected Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Generate the list of targets to run in this task with respect to
     * configuration and provided list of targets.
     *
     * @param config
     * @param targets
     * @return
     */
    protected abstract List<String> generateTargets(Meta config, String... targets);

    /**
     * Gathering of dependencies from workspace
     *
     * @param executor
     * @param workspace
     * @param config
     * @return
     */
    protected abstract DependencySet gather(TaskExecutor executor, Workspace workspace, Meta config, List<String> targets);

    /**
     * The main task body
     *
     * @param executor
     * @param state
     * @param config
     */
    protected abstract void transform(TaskExecutor executor, TaskState state, Meta config);

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
    protected abstract DependencySet<T> result(TaskExecutor executor, Workspace workspace, TaskState state, Meta config);

}
