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
package hep.dataforge.data;

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import java.util.concurrent.CompletableFuture;

/**
 * A piece of data which is basically calculated asynchronously
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 * @param <T>
 */
public interface Data<T> extends Annotated {

    default T get() {
        return getInFuture().join();
    }

    /**
     * Asynchronous data handler. Computation could be canceled if needed
     *
     * @return
     */
    CompletableFuture<T> getInFuture();

    /**
     * Data type. Should be defined before data is calculated.
     *
     * @return
     */
    Class<? super T> dataType();

    default boolean isValid() {
        return !getInFuture().isCancelled();
    }

    @Override
    public default Meta meta() {
        return Meta.empty();
    }

}
