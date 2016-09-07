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

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import org.slf4j.Logger;

/**
 * The action is an independent process that could be performed on one
 * dependency or set of uniform dependencies. The number and naming of results
 * not necessarily is the same as in input.
 *
 *
 * @author Alexander Nozik
 * @param <T> - the main type of input data
 * @param <R> - the main type of resulting object
 */
public interface Action<T, R> extends Named, Encapsulated {

    DataNode<R> run(DataNode<? extends T> data, Meta actionMeta);

   
    default Action<T,R> withLogger(Logger logger) {
        return this;
    }

    default Action<T,R> withParentProcess(String parentProcessName) {
        return this;
    }
    
    default Action<T,R> withContext(Context context) {
        return this;
    }
}
