/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.exceptions.MeasurementException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;

/**
 * A simple one-time measurement wrapping FutureTask. Could be restarted
 *
 * @author Alexander Nozik
 */
public abstract class SimpleMeasurement<T> extends AbstractMeasurement<T> {

    private FutureTask<Pair<T, Instant>> task;

    /**
     * invalidate current task. New task will be created on next getTask call.
     * This method does not guarantee that task is finished when it is cleared
     */
    private void clearTask() {
        task = null;
    }

    /**
     * Perform synchronous measurement
     *
     * @return
     * @throws Exception
     */
    protected abstract T doMeasure() throws Exception;

    @Override
    public synchronized void start() {
        //PENDING do we need executor here?
        //Executors.newSingleThreadExecutor().submit(getTask());
        if (!isStarted()) {
            onStart();
            startTask();
        } else {
            LoggerFactory.getLogger(getClass()).warn("Alredy started");
        }
    }

    @Override
    public synchronized boolean stop(boolean force) {
        if (isStarted()) {
            onFinish();
            return interruptTask(force);
        } else {
            return false;
        }
    }

    protected boolean interruptTask(boolean force) {
        if (task != null) {
            if (task.isCancelled() || task.isDone()) {
                task = null;
                return true;
            } else {
                return task.cancel(force);
            }
        } else {
            return false;
        }
    }

    protected ThreadGroup getThreadGroup() {
        return null;
    }

    protected Duration getMeasurementTimeout() {
        return null;
    }

    protected String getThreadName() {
        return "measurement thread";
    }

    protected void startTask() {
        Runnable process = () -> {
            Pair<T, Instant> res;
            try {
                Duration timeout = getMeasurementTimeout();
                task = buildTask();
                task.run();
                if (timeout == null) {
                    res = task.get();
                } else {
                    res = task.get(getMeasurementTimeout().toMillis(), TimeUnit.MILLISECONDS);
                }
                if (res != null) {
                    result(res.getKey(), res.getValue());
                } else {
                    throw new MeasurementException("Empty result");
                }
            } catch (Exception ex) {
                onError(ex);
            }
            clearTask();
            finishTask();
        };
        new Thread(getThreadGroup(), process, getThreadName()).start();
    }

    /**
     * Reset measurement task and notify listeners
     */
    protected void finishTask() {
        onFinish();
    }

    private FutureTask<Pair<T, Instant>> buildTask() {
        return new FutureTask<>(() -> {
            try {
                T res = doMeasure();
                if (res == null) {
                    return null;
                }
                Instant time = Instant.now();
                return new Pair<>(res, time);
            } catch (Exception ex) {
                onError(ex);
                return null;
            }
        });
    }

}
