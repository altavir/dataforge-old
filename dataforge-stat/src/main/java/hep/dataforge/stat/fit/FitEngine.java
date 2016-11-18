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

import hep.dataforge.io.reports.Logable;
import hep.dataforge.meta.Meta;

/**
 * <p>
 * FitEngine interface.</p>
 *
 * @author Alexander Nozik
 */
public interface FitEngine {

    /**
     *
     * @param name
     * @return
     */
    static FitEngine forName(String name) {
        return FitEngineBuilder.buildEngine(name);
    }

    /**
     * Run the fit with given fit state and fit task
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     * @param task a {@link hep.dataforge.stat.fit.FitStage} object.
     * @param log
     * @return a {@link hep.dataforge.stat.fit.FitTaskResult} object.
     */
    FitTaskResult run(FitState state, FitStage task, Logable log);

    /**
     *
     * @param state
     * @param taskAnnotation
     * @param log
     * @return
     */
    default FitTaskResult run(FitState state, Meta taskAnnotation, Logable log) {
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
