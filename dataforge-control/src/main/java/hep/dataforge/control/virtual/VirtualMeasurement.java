/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.virtual;

import hep.dataforge.control.measurements.SimpleMeasurement;
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 *
 * @author Alexander Nozik
 */
public class VirtualMeasurement<T> extends SimpleMeasurement<T> {
    
    Duration delay;
    Callable<T> result;

    public VirtualMeasurement(Duration delay, Callable<T> result) {
        this.delay = delay;
        this.result = result;
    }

    @Override
    protected T doMeasure() throws Exception {
        Thread.sleep(delay.toMillis());
        return result.call();
    }
    
    
}