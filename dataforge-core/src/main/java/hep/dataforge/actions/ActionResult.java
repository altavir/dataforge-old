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

import hep.dataforge.dependencies.Data;
import hep.dataforge.io.log.Log;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * The asynchronous result of the action
 *
 * @author Alexander Nozik
 * @param <R>
 */
public class ActionResult<R> implements Data<R>{
    
    private final Log log;
    private final CompletableFuture<R> future;
    private final Class<R> type;

    public ActionResult(Class<R> type, Log log, Supplier<R> supplier, Executor executor) {
        this.log = log;
        this.type = type;
        this.future = CompletableFuture.supplyAsync(supplier, executor);
    }
    
    public ActionResult(Class<R> type, Log log, CompletableFuture<R> future) {
        this.log = log;
        this.type = type;
        this.future = future;
    }    

    public Log log(){
        return log;
    }

    @Override
    public CompletableFuture<R> getInFuture() {
        return future;
    }

    @Override
    public Class<R> dataType() {
        return type;
    }
}
