/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.virtual;

import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.control.measurements.Sensor;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;
import java.time.Duration;
import java.util.Random;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper methods to create virtual devices and measurements
 *
 * @author Alexander Nozik
 */
public class Virtual {

    private static final Random generator = new Random();

    /**
     * returns fixed double after given duration
     *
     * @param delay
     * @param result
     * @return
     */
    public static Measurement<Double> constantDoubleMeasurement(Duration delay, Double result) {
        return new VirtualMeasurement<>(delay, () -> result);
    }

    /**
     * Returns
     *
     * @param delay
     * @param mean
     * @param sigma
     * @return
     */
    public static Measurement<Double> randomDoubleMeasurement(Duration delay, double mean, double sigma) {
        return new VirtualMeasurement<>(delay, () -> {
            return generator.nextGaussian() * sigma + mean;
        });
    }

    public static <T> Sensor<T> virtualSensor(String name, Context context, Meta meta, Function<Meta, Measurement<T>> factory) {
        return new Sensor<T>(name, context, meta) {
            @Override
            protected Measurement<T> createMeasurement() {
                return factory.apply(meta());
            }

            @Override
            public String type() {
                return "Virtual sensor";
            }
        };
    }

    public static Sensor<Double> randomDoubleSensor(String name, Context context, Meta meta, Duration delay, double mean, double sigma) {
        Sensor<Double> sensor = new Sensor<Double>(name, context, meta) {
            @Override
            protected Measurement<Double> createMeasurement() {
                return randomDoubleMeasurement(delay, mean, sigma);
            }

            @Override
            public String type() {
                return "Virtual sensor";
            }
        };
        try {
            sensor.init();
        } catch (ControlException ex) {
            throw new Error(ex);
        }
        return sensor;
    }

    public static Sensor<Double> randomDoubleSensor(String name, Duration delay, double mean, double sigma) {
        return randomDoubleSensor(name, GlobalContext.instance(), null, delay, mean, sigma);
    }
}
