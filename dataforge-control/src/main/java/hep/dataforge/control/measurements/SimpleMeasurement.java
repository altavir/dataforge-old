/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import java.time.Instant;
import java.util.concurrent.FutureTask;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;

/**
 * A simple one-time measurement wrapping FutureTask. Could be restarted
 *
 * @author Alexander Nozik
 */
public abstract class SimpleMeasurement<T> extends AbstractMeasurement<T> {

    private FutureTask<Pair<T, Instant>> task;

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
                onFinish();
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
    public synchronized void start() {
        //PENDING do we need executor here?
        //Executors.newSingleThreadExecutor().submit(getTask());
        if (!isStarted()) {
            onStart();
            getTask().run();
        } else {
            LoggerFactory.getLogger(getClass()).warn("Alredy started");
        }
    }

    @Override
    public synchronized boolean stop(boolean force) {
        if (isStarted()) {
            onFinish();
            return getTask().cancel(force);
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
