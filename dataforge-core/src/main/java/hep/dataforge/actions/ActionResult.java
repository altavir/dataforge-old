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
package hep.dataforge.actions;

import hep.dataforge.computation.Goal;
import hep.dataforge.data.Data;
import hep.dataforge.io.reports.Report;
import hep.dataforge.meta.Meta;

/**
 * The asynchronous result of the action
 *
 * @author Alexander Nozik
 * @param <R>
 */
public class ActionResult<R> extends Data<R> {

    private final Report log;

    public ActionResult(Report log, Goal<R> goal, Meta meta, Class<R> type) {
        super(goal, type, meta);
        this.log = log;
    }

    public Report log() {
        return log;
    }
}
