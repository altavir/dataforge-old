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

import hep.dataforge.computation.ProgressCallback;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.io.reports.Report;
import hep.dataforge.meta.Meta;

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
public abstract class MultiStageTask<R> extends AbstractTask<R> {

    @Override
    protected DataNode<R> run(TaskModel model, ProgressCallback callback, DataNode<?> data) {
        Context context = model.getWorkspace().getContext();
        Report report = new Report(getName(), context.getReport());
        MultiStageTaskState state = new MultiStageTaskState(data, report);

        getLogger().debug("Starting transformation phase");
        callback.updateMessage("Data transformation...");
        transform(callback, context, state, model.meta());
        if (!state.isFinished) {
            getLogger().warn("Task state is not finalized. Using last applied state as a result");
            state.finish();
        }
        getLogger().debug("Starting result phase");

        callback.updateMessage("Task result generation...");
        DataNode<R> res = result(model, state);

        return res;
    }

    /**
     * The main task body
     *
     * @param state
     * @param config
     */
    protected abstract void transform(ProgressCallback callback, Context context, MultiStageTaskState state, Meta config);

    /**
     * Generating finish and storing it in workspace.
     *
     * @param state
     * @return
     */
    protected DataNode<R> result(TaskModel model, MultiStageTaskState state) {
        //FIXME check for type cast
        return (DataNode<R>) state.getResult();
    }
}
