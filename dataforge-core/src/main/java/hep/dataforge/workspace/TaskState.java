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
import java.util.List;
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
     * List of targets for this task
     */
    private List<String> targets;
    /**
     * list of stages results
     */
    private final Map<String, DataNode> stages = new HashMap<>();
    /**
     * final result of task
     */
    private final Map<String, TaskResult> result = new HashMap<>();
    boolean isFinal = false;

    private TaskState() {
    }

    public TaskState(DataNode data, List<String> targets) {
        this.stages.put(INITAIL_DATA_STAGE, data);
        if (targets != null) {
            this.targets = targets;
        } else {
            this.targets = Collections.emptyList();
        }
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

    public Map<String, TaskResult> getResult() {
        return Collections.unmodifiableMap(result);
    }

    public TaskState setData(String stage, DataNode data) {
        if (isFinal) {
            throw new IllegalStateException("Can't edit task state after result is finalized");
        } else {
            this.stages.put(stage, data);
            return this;
        }
    }

    public synchronized TaskState putResult(String target, TaskResult result) {
        if (isFinal) {
            throw new IllegalStateException("Can't edit task state after result is finalized");
        } else {
            this.result.put(target, result);
            return this;
        }
    }

    public TaskState finish() {
        this.isFinal = true;
        return this;
    }

    public List<String> getTargets() {
        return targets;
    }

}
