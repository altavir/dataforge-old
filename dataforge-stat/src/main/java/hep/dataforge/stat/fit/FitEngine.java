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
package hep.dataforge.stat.fit;

import hep.dataforge.io.history.History;
import hep.dataforge.meta.Meta;

/**
 * <p>
 * FitEngine interface.</p>
 *
 * @author Alexander Nozik
 */
public interface FitEngine {

    /**
     * Run the fit with given fit state and fit task
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     * @param task a {@link hep.dataforge.stat.fit.FitStage} object.
     * @param log
     * @return a {@link FitResult} object.
     */
    FitResult run(FitState state, FitStage task, History log);

    /**
     *
     * @param state
     * @param taskAnnotation
     * @param log
     * @return
     */
    default FitResult run(FitState state, Meta taskAnnotation, History log) {
        return run(state, new FitStage(taskAnnotation), log);
    }

    /**
     *
     * @param state
     * @param task
     * @return
     */
    default String[] getFitPars(FitState state, FitStage task) {
        String[] res = task.getFreePars();
        if (res == null || res.length == 0) {
            res = state.getModel().namesAsArray();
        }
        return res;
    }
}
