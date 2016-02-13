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
public abstract class RegularMeasurement<T> extends SimpleMeasurement<T> {

    private boolean stopFlag = false;

    @Override
    protected FutureTask<Pair<T, Instant>> buildTask() {
        //FIXME refactor using FutureTask runAndReset
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
                if (stopFlag || (stopOnError() && getState() == MeasurementState.FAILED)) {
                    onFinish();
                } else {
                    //Task is null so it is reset automaically
                    getTask().run();
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
            stopFlag = true;
            return true;
        }
    }

    protected boolean stopOnError() {
        return true;
    }

    protected abstract Duration getDelay();

}
