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

import hep.dataforge.content.Named;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.dependencies.Dependency;
import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import java.util.concurrent.ExecutionException;

/**
 * A place to store tasks and their results
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Workspace extends Annotated, Named, Encapsulated {

    /**
     * Check task dependencies and run it with given configuration
     *
     * @param taskName
     * @param config
     * @return
     */
    DependencySet runTask(String taskName, Meta config, String... targets) throws InterruptedException, ExecutionException;

//    /**
//     * Run task for specific target.
//     *
//     * @param <T>
//     * @param taskName
//     * @param config
//     * @param target
//     * @return
//     */
//    <T> Dependency<T> runTask(String taskName, Meta config, String target);
    /**
     * Check if workspace has static data dependency with given stage and name.
     * Stages are used for complex workspaces and could be completely omitted
     * for simple ones (use empty string or null for default stage).
     *
     * @param stage a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean hasData(String stage, String name);

    /**
     * Get the static data dependency from specific stage. Stages are used for
     * complex workspaces and could be completely omitted for simple ones (use
     * empty string or null for default stage).
     *
     * @param stage a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Dependency getData(String stage, String name);

//    <T> void putData(String name, T data);
//    
//    <T extends Named> void putData(T data);
    /**
     * Get the dependency resolver for this workspace
     *
     * @return
     */
    DependencyResolver getDependencyResolver();

    //TODO replace by ThreadFactory
    ThreadGroup getWorkspaceThreadGroup();

    /**
     * Notify the workspace that task is complete
     *
     * @param taskName
     * @param taskConfig
     * @param target
     * @param taskResult
     */
    void notifyTaskComplete(TaskResult result);

    /**
     * Get the identity for this workspace including context identity
     *
     * @return
     */
    Identity getIdentity();

}
