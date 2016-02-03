/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.utils.ReferenceRegistry;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.util.Pair;

/**
 * A measurement wrapping a FutureTask. Could be restarted if needed
 *
 * @author Alexander Nozik
 */
public abstract class SimpletMeasurement<T> extends AbstractMeasurement<T> {

    private FutureTask<Pair<T, Instant>> task;

    @Override
    protected Pair<T, Instant> get() throws MeasurementException {
        if (this.lastResult != null) {
            return this.lastResult;
        } else {
            try {
                return getTask().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new MeasurementException(exception);
            }
        }
    }

    protected FutureTask<Pair<T, Instant>> buildTask() {
        return new FutureTask<>(() -> {
            try {
                T res = doMeasure();
                Instant time = Instant.now();
                result(res, time);
                return new Pair<>(res, time);
            } catch (Exception ex) {
                onError(ex);
                return null;
            } finally {
                clearTask();
                onStop();
            }
        });
    }

    /**
     * invalidate current task. New task will be created on next getTask call.
     * This method does not guarantee that task is finished when it is cleared
     */
    protected final void clearTask() {
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
    public void start() {
        //PENDING do we need executor here?
        //Executors.newSingleThreadExecutor().submit(getTask());
        runTask();
        onStart();
    }

    protected void runTask() {
        getTask().run();
    }

    @Override
    public boolean stop(boolean force) {
        if (getTask().cancel(force)) {
            return true;
        } else {
            return false;
        }
    }

    protected FutureTask<Pair<T, Instant>> getTask() {
        if (task == null) {
            task = buildTask();
        }
        return task;
    }

}
