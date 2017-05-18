/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.control.devices.AbstractMeasurementDevice;
import hep.dataforge.control.devices.StateDef;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.values.Value;

import java.util.concurrent.Callable;

/**
 * A device with single one-time or periodic measurement
 *
 * @param <T>
 * @author Alexander Nozik
 */
@StateDef(name = "inProgress", info = "Shows if this sensor is actively measuring")
public abstract class Sensor<T> extends AbstractMeasurementDevice<T> {

    private Measurement<T> measurement;

    /**
     * Create simple sensor with simple one-time measurement
     *
     * @param <T>
     * @param proc
     * @return
     */
    public static <T> Sensor<T> simpleSensor(Callable<T> proc) {
        return new Sensor<T>() {
            @Override
            protected Measurement<T> createMeasurement() throws MeasurementException {
                return new SimpleMeasurement<T>() {
                    @Override
                    protected T doMeasure() throws Exception {
                        return proc.call();
                    }
                };
            }
        };
    }

    /**
     * Read sensor data synchronously
     *
     * @return
     * @throws hep.dataforge.exceptions.MeasurementException
     */
    public synchronized T read() throws MeasurementException {
        return startMeasurement().getResult();
    }

    public Measurement<T> startMeasurement() throws MeasurementException {
        if (this.measurement == null || this.measurement.isFinished()) {
            this.measurement = createMeasurement();
            measurement.addListener(this);
            onCreateMeasurement(measurement);
        } else if (measurement.isStarted()) {
            getLogger().warn("Trying to start next measurement on sensor while previous measurement is active. Ignoring.");
        }

        this.measurement.start();
        return this.measurement;
    }

    public Measurement<T> getMeasurement() {
        return measurement;
    }

    /**
     * Stop current measurement
     *
     * @param force if true than current measurement will be interrupted even if
     *              running
     * @throws hep.dataforge.exceptions.MeasurementException
     */
    public void stopMeasurement(boolean force) throws MeasurementException {
        if (this.measurement != null && !this.measurement.isFinished()) {
            this.measurement.stop(force);
        } else {
            getLogger().warn("No active measurement to stop. Ignoring.");
        }
    }

//    @Override
//    protected <T> void onFinishMeasurement(Measurement<T> measurement) {
//        measurement = null;
//    }

    @Override
    public void shutdown() throws ControlException {
        if (measurement != null) {
            measurement.stop(true);
        }
        super.shutdown();
    }

    /**
     * Shows if there is ongoing measurement
     *
     * @return
     */
    public boolean isMeasuring() {
        return measurement != null && !measurement.isFinished();
    }

    @Override
    public Value getState(String stateName) {
        if ("measuring".equals(stateName)) {
            return Value.of(isMeasuring());
        } else {
            return super.getState(stateName);
        }
    }

    protected abstract Measurement<T> createMeasurement() throws MeasurementException;

}
