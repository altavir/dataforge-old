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

import hep.dataforge.exceptions.MeasurementException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A scheduler that performs the given task with fixed intervals. The next task
 * is scheduled only after the previous one is finished to avoid stacking.
 *
 * @author Alexander Nozik
 * @param <V>
 */
public class TaskScheduler<V> {

    private ScheduledFuture scheduleTask;
    private final ScheduledExecutorService scheduler;
    private final ControlTask<V> task;
    private final Duration timeout;
    private final ConcurrentLinkedDeque<HistoryEntry> history = new ConcurrentLinkedDeque<>();
    private int maxHistoryLength = 100;

    public TaskScheduler(ScheduledExecutorService scheduler, ControlTask<V> task) {
        this.scheduler = scheduler;
        this.task = task;
        timeout = null;
    }

    public TaskScheduler(ScheduledExecutorService scheduler, ControlTask<V> task, Duration timeout) {
        this.scheduler = scheduler;
        this.task = task;
        this.timeout = timeout;
    }

    public V lastValue() {
        if (history.isEmpty()) {
            return null;
        } else {
            return history.getLast().value;
        }
    }

    public Instant lastTimeStamp() {
        if (history.isEmpty()) {
            return null;
        } else {
            return history.getLast().time;
        }
    }

    public Map<Instant, V> getHistory() {
        Map<Instant, V> res = new HashMap<>();
        history.stream().forEach((entry) -> {
            res.put(entry.time, entry.value);
        });
        return res;
    }

    public int getMaxHistoryLength() {
        return maxHistoryLength;
    }

    public void setMaxHistoryLength(int maxHistoryLength) {
        this.maxHistoryLength = maxHistoryLength;
    }

    public void start(Duration delay) {
        if (isStarted()) {
            scheduleTask.cancel(false);
        }
        schedule(delay);
    }

    public void stop() {
        scheduleTask.cancel(false);
        scheduleTask = null;
    }

    /**
     * The method could be overriden to ensure the process continues until some
     * specific condition is satisfied.
     *
     * @param lastValue
     * @return
     */
    protected boolean continueAlowed(V lastValue) {
        return true;
    }

    protected void pushToHistory(Instant time, V value) {
        history.add(new HistoryEntry(time, value));
        // clearing old history entries
        while (history.size() > maxHistoryLength) {
            history.removeFirst();
        }
    }

    private void schedule(Duration delay) {
        Runnable call = () -> {

            try {
                task.run();
                V res;
                if (timeout == null) {
                    res = task.get();
                } else {
                    res = task.get(timeout);
                }

                try {
                    pushToHistory(task.getTime(), res);
                } catch (MeasurementException ex) {
                    throw new Error("Unreachable statement reached");
                }

            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                //TODO сделать обработку ошибки
                throw new RuntimeException(ex);
            }

        };
        scheduleTask = scheduler.scheduleWithFixedDelay(call, 0, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean isStarted() {
        return scheduleTask != null;
    }

    private class HistoryEntry {

        public HistoryEntry(Instant time, V value) {
            this.time = time;
            this.value = value;
        }

        Instant time;
        V value;
    }

}
