/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.exceptions.MeasurementException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javafx.util.Pair;

/**
 *
 * @author Alexander Nozik
 */
public abstract class RegularMeasurement<T> extends AbstractMeasurement<T> {

//    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
//    private Future<?> process;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<Pair<T, Instant>> measurementTask;
    private volatile boolean stopFlag = false;

    @Override
    public void start() {
        if (measurementTask == null || measurementTask.isDone()) {
            stopFlag = false;
            measurementTask = buildTask();
            executor.submit(buildTask());
        }
        notifyStarted();
    }

    @Override
    public boolean stop(boolean force) {
        if (force) {
            if (this.measurementTask != null) {
                this.measurementTask.cancel(force);
                this.measurementTask = null;
                notifyStopped();
                return true;
            } else {
                return false;
            }
        } else {
            stopFlag = true;
            notifyStopped();
            return true;
        }
    }

    private T performMeasutement() {
        try {
            return doMeasurement();
        } catch (Exception ex) {
            fail(ex);
            if (stopOnError()) {
                stop(true);
            }
            return null;
        }
    }

    @Override
    protected Future<Pair<T, Instant>> measurementTask() {
        return this.measurementTask;
    }

    @Override
    public boolean isFinished() {
        return this.stopFlag == true;
    }

    private FutureTask<Pair<T, Instant>> buildTask() {
        return new FutureTask<>(() -> {
            //delayed measurement
            Thread.sleep(getDelay().toMillis());
            T result = performMeasutement();
            Instant now = Instant.now();
            if (result != null) {
                result(result, now);
                if (!stopFlag) {
                    measurementTask = buildTask();
                    executor.submit(buildTask());
                }
                return new Pair<>(result, now);
            } else {
                return null;
            }
        });
    }

    /**
     * Do measurement and call {@code result} after it is finished
     *
     * @throws MeasurementException
     */
    protected abstract T doMeasurement() throws Exception;

    protected boolean stopOnError() {
        return true;
    }

    protected abstract Duration getDelay();

}
