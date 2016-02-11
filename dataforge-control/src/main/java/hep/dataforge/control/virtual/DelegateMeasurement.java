/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.virtual;

import hep.dataforge.control.measurements.SimpletMeasurement;
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 *
 * @author Alexander Nozik
 */
public class DelegateMeasurement<T> extends SimpletMeasurement<T> {
    
    Duration delay;
    Callable<T> result;

    public DelegateMeasurement(Duration delay, Callable<T> result) {
        this.delay = delay;
        this.result = result;
    }

    @Override
    protected T doMeasure() throws Exception {
        Thread.sleep(delay.toMillis());
        return result.call();
    }
    
    
}
