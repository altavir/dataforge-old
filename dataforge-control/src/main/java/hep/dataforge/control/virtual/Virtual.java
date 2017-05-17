/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.virtual;

import hep.dataforge.control.devices.StateDef;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.control.measurements.Sensor;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

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

    public static <T> Sensor<T> virtualSensor(Function<Meta, Measurement<T>> factory, StateDef... states) {
        return new Sensor<T>() {
            @Override
            protected Measurement<T> createMeasurement() {
                return factory.apply(meta());
            }

            @Override
            public String type() {
                return "Virtual sensor";
            }

            @Override
            public List<StateDef> stateDefs() {
                return Arrays.asList(states);
            }
        };
    }

    public static <T> Sensor<T> virtualSensor(Function<Meta, Measurement<T>> factory) {
        return new Sensor<T>() {
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

    public static Sensor<Double> randomDoubleSensor(String name, Duration delay, double mean, double sigma) {
        Sensor<Double> sensor = new Sensor<Double>() {
            {
                setMeta(new MetaBuilder("device").setValue("name", name).build());
            }

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

}
