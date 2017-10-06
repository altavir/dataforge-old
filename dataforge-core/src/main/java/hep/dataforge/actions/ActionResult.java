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

import hep.dataforge.data.NamedData;
import hep.dataforge.goals.Goal;
import hep.dataforge.io.history.Chronicle;
import hep.dataforge.meta.Meta;

/**
 * The asynchronous result of the action
 *
 * @author Alexander Nozik
 * @param <R>
 */
public class ActionResult<R> extends NamedData<R> {

    private final Chronicle log;

    public ActionResult(String name, Class<R> type, Goal<R> goal, Meta meta, Chronicle log) {
        super(name, goal, type, meta);
        this.log = log;
    }

    public Chronicle log() {
        return log;
    }
}
