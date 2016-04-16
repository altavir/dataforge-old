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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import hep.dataforge.data.DataNode;

/**
 * The mutable data content of a task.
 *
 * @author Alexander Nozik
 */
public class TaskState {

    private static final String INITAIL_DATA_STAGE = "@init";

    /**
     * list of stages results
     */
    private final Map<String, DataNode> stages = new HashMap<>();
    /**
     * final result of task
     */
    private DataNode result;
    boolean isFinal = false;

    private TaskState() {
    }

    public TaskState(DataNode data) {
        this.stages.put(INITAIL_DATA_STAGE, data);
    }

    public DataNode getData(String stage) {
        return stages.get(stage);
    }

    /**
     * Return initial data
     *
     * @return
     */
    public DataNode getData() {
        return getData(INITAIL_DATA_STAGE);
    }

    public DataNode getResult() {
        return result;
    }

    public TaskState setData(String stage, DataNode data) {
        if (isFinal) {
            throw new IllegalStateException("Can't edit task state after result is finalized");
        } else {
            this.stages.put(stage, data);
            return this;
        }
    }

    public synchronized TaskState result(DataNode result) {
        if (isFinal) {
            throw new IllegalStateException("Can't edit task state after result is finalized");
        } else {
            this.result = result;
            return this;
        }
    }

    public TaskState finish() {
        this.isFinal = true;
        return this;
    }

}
