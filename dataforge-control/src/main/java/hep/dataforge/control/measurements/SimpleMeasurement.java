/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javafx.util.Pair;

/**
 * A measurement wrapper for simple single run
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public abstract class SimpleMeasurement<T> extends AbstractMeasurement<T> {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<Pair<T, Instant>> measurementTask;

    @Override
    public boolean isFinished() {
        return this.measurementTask.isDone() || this.measurementTask.isCancelled();
    }

    protected Future<Pair<T, Instant>> getTask() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected Pair<T, Instant> doGet() throws Exception {
        return getTask().get();
    }    

    @Override
    public void start() {
        if (measurementTask == null || measurementTask.isDone()) {
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
            notifyStopped();
            return true;
        }
    }

    private FutureTask<Pair<T, Instant>> buildTask() {
        return new FutureTask<>(() -> {
            T result = performMeasurement();
            Instant now = Instant.now();
            if (result != null) {
                result(result, now);
                return new Pair<>(result, now);
            } else {
                return null;
            }
        });
    }

    private T performMeasurement() {
        try {
            return doMeasurement();
        } catch (Exception ex) {
            fail(ex);
            return null;
        }
    }

    /**
     * Do measurement 
     *
     * @throws MeasurementException
     */
    protected abstract T doMeasurement() throws Exception;
}
