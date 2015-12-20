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
package hep.dataforge.control.tasks;

import hep.dataforge.events.EventHandler;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.exceptions.MeasurementNotReadyException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Alexander Nozik
 * @param <V>
 */
public class ControlTask<V> extends FutureTask<V> implements Signal<V> {
    
    public static <V> ControlTask<V> immediateResult(String tag, EventHandler handler, Instant time, V result){
        return new ControlTask<>(tag, handler, time, () -> result);
    }

    private EventHandler handler;
    private final String tag;

    protected EventHandler getHandler() {
        return handler;
    }

    @Override
    public String getTag() {
        return tag;
    }
    private Instant time = null;

    /**
     *
     * @param tag the source tag for this Control task. The source tag used to
     * filter events
     * @param handler
     * @param clbl
     */
    public ControlTask(String tag, EventHandler handler, Callable<V> clbl) {
        super(clbl);
        this.tag = tag;
        this.handler = handler;
    }

    protected ControlTask(String tag, EventHandler handler, Instant time, Callable<V> clbl) {
        super(clbl);
        this.tag = tag;
        this.handler = handler;
        this.time = time;
    }

    public ControlTask(String tag, EventHandler handler, Runnable r, V v) {
        super(r, v);
        this.tag = tag;
        this.handler = handler;
    }

    /**
     * Set the listener for control events from this task
     *
     * @param handler
     */
    protected void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void done() {
        super.done(); //To change body of generated methods, choose Tools | Templates.
        if (time == null) {
            time = Instant.now();
        }
        if (handler != null) {
            try {
                handler.handle(new TaskCompleteEvent<>(tag, time, get()));
            } catch (InterruptedException | ExecutionException ex) {
                setException(ex);
            }
        }
    }

    @Override
    protected void setException(Throwable thrwbl) {
        super.setException(thrwbl); //To change body of generated methods, choose Tools | Templates.
        //TODO добавить событие прерывания?
    }

    /**
     * Adapter to use with Java 8 time
     *
     * @param duration
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Override
    public V get(Duration duration) throws InterruptedException, ExecutionException, TimeoutException {
        return super.get(duration.get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel the task and
     *
     * @param bln
     * @return
     */
    @Override
    public boolean cancel(boolean bln) {
        if (handler != null) {
            handler.handle(new TaskInterruptedEvent(tag, Instant.now()));
        }
        return super.cancel(bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Instant getTime() throws MeasurementException {
        if (isDone()) {
            return time;
        } else {
            throw new MeasurementNotReadyException();
        }
    }

    /**
     * Set specific time of task completion
     *
     * @param time
     */
    protected void setTime(Instant time) {
        this.time = time;
    }

}
