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
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

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

    protected final Class<R> type;

    public MultiStageTask(Class<R> type) {
        this.type = type;
    }

    @Override
    protected DataNode<R> run(TaskModel model, DataNode<?> data) {
        MultiStageTaskState state = new MultiStageTaskState(data);
        Logger logger = getLogger(model);
//        Work work = getWork(model, data.getName());

        logger.debug("Starting transformation phase");
//        work.setStatus("Data transformation...");
        transform(model, state);
        if (!state.isFinished) {
            logger.warn("Task state is not finalized. Using last applied state as a result");
            state.finish();
        }
        logger.debug("Starting result phase");

//        work.setStatus("Task result generation...");
        DataNode<R> res = result(model, state);

        return res;
    }

    /**
     * The main task body
     *
     * @param model
     * @param state
     */
    protected abstract void transform(TaskModel model, MultiStageTaskState state);

    /**
     * Generating finish and storing it in workspace.
     *
     * @param state
     * @return
     */
    protected DataNode<R> result(TaskModel model, MultiStageTaskState state) {
        return state.getResult().checked(type);
    }

    /**
     * The mutable data content of a task.
     *
     * @author Alexander Nozik
     */
    public static class MultiStageTaskState {

        private static final String INITAIL_DATA_STAGE = "@data";

        /**
         * list of stages results
         */
        private final Map<String, DataNode> stages = new LinkedHashMap<>();
        boolean isFinished = false;
        /**
         * final finish of task
         */
        private DataNode result;

        private MultiStageTaskState() {
        }

        public MultiStageTaskState(DataNode data) {
            this.stages.put(INITAIL_DATA_STAGE, data);
        }

        public DataNode<?> getData(String stage) {
            return stages.get(stage);
        }

        /**
         * Return initial data
         *
         * @return
         */
        public DataNode<?> getData() {
            return getData(INITAIL_DATA_STAGE);
        }

        public DataNode<?> getResult() {
            return result;
        }

        public MultiStageTaskState setData(String stage, DataNode data) {
            if (isFinished) {
                throw new IllegalStateException("Can't edit task state after result is finalized");
            } else {
                this.stages.put(stage, data);
                result = data;
                return this;
            }
        }

        public synchronized MultiStageTaskState finish(DataNode result) {
            if (isFinished) {
                throw new IllegalStateException("Can't edit task state after result is finalized");
            } else {
                this.result = result;
                isFinished = true;
                return this;
            }
        }

        public MultiStageTaskState finish() {
            this.isFinished = true;
            return this;
        }

    }
}
