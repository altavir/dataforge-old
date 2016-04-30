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

import hep.dataforge.data.Data;
import hep.dataforge.meta.Meta;

/**
 * A generalization of result of the task. It could be lazy!
 *
 * @author Alexander Nozik
 * @param <T>
 */
public interface TaskResult<T> extends Data<T> {

    /**
     * The taskName of the task which produced this result
     *
     * @return a {@link java.lang.String} object.
     */
    String taskName();

    /**
     * The taskName of the target for this result. The taskName of the result is
     * by default equals the taskName of the target, or taskName of input
     * content but it is not mandatory. Could be empty.
     *
     * @return a {@link java.lang.String} object.
     */
    String target();

    /**
     * The configuration used to produce this task result.
     *
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    Meta taskConfig();
}
