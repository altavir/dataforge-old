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
package hep.dataforge.control.ports;

import hep.dataforge.exceptions.PortException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * @author Alexander Nozik
 */
public abstract class VirtualPort extends PortHandler {

    private final Set<TaggedFuture> futures = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);

    public VirtualPort(String portName) {
        super(portName);
    }

    @Override
    public void send(String message) throws PortException {
        evaluateRequest(message);
    }
    
    /**
     * The device logic here
     * @param request 
     */
    protected abstract void evaluateRequest(String request);
    
    protected synchronized void clearCompleted(){
        futures.stream().filter((future) -> (future.future.isDone() || future.future.isCancelled())).forEach((future) -> {
            futures.remove(future);
        });
    }
    
    protected synchronized void cancelByTag(String tag){
        futures.stream().filter((future)->future.hasTag(tag)).forEach((future)->future.cancel());
    }

    /**
     * Plan the response with given delay
     * @param response
     * @param delay
     * @param tags 
     */
    protected synchronized void planResponse(String response, Duration delay, String... tags) {
        clearCompleted();
        Runnable task = ()-> recievePhrase(response);
        ScheduledFuture future = scheduler.schedule(task, delay.toNanos(), TimeUnit.NANOSECONDS);
        this.futures.add(new TaggedFuture(future, tags));
    }
    
    protected synchronized void planRegularResponse(Supplier<String> responseBuilder, Duration delay, Duration period, String... tags) {
        clearCompleted();
        Runnable task = ()-> recievePhrase(responseBuilder.get());
        ScheduledFuture future = scheduler.scheduleAtFixedRate(task, delay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS);
        this.futures.add(new TaggedFuture(future, tags));
    }    

    private class TaggedFuture {

        public TaggedFuture(ScheduledFuture future, String... tags) {
            this.future = future;
            this.tags = new HashSet<>();
            this.tags.addAll(Arrays.asList(tags));
        }

        ScheduledFuture future;
        Set<String> tags;

        public boolean hasTag(String tag) {
            return tags.contains(tag);
        }

        public boolean cancel() {
            return future.cancel(true);
        }
    }
}
