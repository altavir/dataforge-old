/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.FutureTask;
import javafx.util.Pair;

/**
 *
 * @author Alexander Nozik
 */
public abstract class RegularMeasurement<T> extends SimpletMeasurement<T> {

    @Override
    protected void onError(Throwable error) {
        super.onError(error);
        if (stopOnError()) {
            isFinished = true;
        }
    }

    @Override
    protected FutureTask<Pair<T, Instant>> buildTask() {
        return new FutureTask<>(() -> {
            try {
                Thread.sleep(getDelay().toMillis());
                T res = doMeasure();
                Instant time = Instant.now();
                result(res, time);
                return new Pair<>(res, time);
            } catch (Exception ex) {
                onError(ex);
                return null;
            } finally {
                clearTask();
                if (isFinished) {
                    onStop();
                } else {
                    runTask();
                }
            }
        });
    }

    @Override
    public boolean stop(boolean force) {
        if (isFinished()) {
            return false;
        } else if (force) {
            return getTask().cancel(force);
        } else {
            isFinished = true;
            return true;
        }
    }

    protected boolean stopOnError() {
        return true;
    }

    protected abstract Duration getDelay();

}
