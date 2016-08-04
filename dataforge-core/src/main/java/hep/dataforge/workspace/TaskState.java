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
import hep.dataforge.io.reports.Report;
import hep.dataforge.io.reports.Reportable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The mutable data content of a task.
 *
 * @author Alexander Nozik
 */
public class TaskState implements Reportable {

    private static final String INITAIL_DATA_STAGE = "@data";

    /**
     * list of stages results
     */
    private final Map<String, DataNode> stages = new LinkedHashMap<>();
    /**
     * final finish of task
     */
    private DataNode result;
    boolean isFinished = false;

    private Report report;

    private TaskState() {
    }

    public TaskState(DataNode data, Report report) {
        this.stages.put(INITAIL_DATA_STAGE, data);
        this.report = report;
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

    public TaskState setData(String stage, DataNode data) {
        if (isFinished) {
            throw new IllegalStateException("Can't edit task state after result is finalized");
        } else {
            this.stages.put(stage, data);
            result = data;
            return this;
        }
    }

    public synchronized TaskState finish(DataNode result) {
        if (isFinished) {
            throw new IllegalStateException("Can't edit task state after result is finalized");
        } else {
            this.result = result;
            isFinished = true;
            return this;
        }
    }

    public TaskState finish() {
        this.isFinished = true;
        return this;
    }
//
//    public void setReport(Report report) {
//        this.report = report;
//    }

    @Override
    public Report getReport() {
        if (report == null) {
            report = new Report("taskState");
        }
        return report;
    }

}
