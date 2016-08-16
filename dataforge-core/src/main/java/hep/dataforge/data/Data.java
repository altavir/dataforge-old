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

import hep.dataforge.computation.Goal;
import hep.dataforge.computation.StaticGoal;
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
public class Data<T> implements Annotated {
    
    private final Goal<T> goal;
    private final Meta meta;
    private final Class<T> type;

    public Data(Goal<T> goal, Class<T> type, Meta meta) {
        this.goal = goal;
        this.meta = meta;
        this.type = type;
    }
    public Data(Goal<T> goal, Class<T> type) {
        this.goal = goal;
        this.meta = Meta.empty();
        this.type = type;
    }

    public static <T> Data<T> buildStatic(T content, Meta meta) {
        return new Data(new StaticGoal(content), content.getClass(), meta);
    }

    public static <T> Data<T> buildStatic(T content) {
        return buildStatic(content, Meta.empty());
    }

    public Goal<T> getGoal() {
        return goal;
    }

    /**
     * Compute underlying goal and return sync result.
     * @return 
     */
    public T get() {
        goal.start();
        return goal.result().join();
    }

    /**
     * Asynchronous data handler. Computation could be canceled if needed
     *
     * @return
     */
    public CompletableFuture<T> getInFuture(){
        return goal.result();
    }

    /**
     * Data type. Should be defined before data is calculated.
     *
     * @return
     */
    public Class<T> type() {
        return type;
    }

    public boolean isValid() {
        return !getInFuture().isCancelled() && !getInFuture().isCompletedExceptionally();
    }

    @Override
    public Meta meta() {
        return meta;
    }

}
