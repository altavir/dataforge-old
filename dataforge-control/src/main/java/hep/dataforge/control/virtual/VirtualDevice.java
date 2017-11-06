/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.virtual;

import hep.dataforge.context.Context;
import hep.dataforge.control.devices.Device;
import hep.dataforge.control.devices.Sensor;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;

import java.time.Duration;
import java.util.Random;

/**
 * Helper methods to create virtual devices and measurements
 *
 * @author Alexander Nozik
 */
public class VirtualDevice {
    public static final String VIRTUAL_SENSOR_TYPE = "@test";

    private static final Random generator = new Random();

    /**
     * returns fixed double after given duration
     *
     * @param delay
     * @param result
     * @return
     */
    public static Measurement<Double> constantDoubleMeasurement(Device device, Duration delay, Double result) {
        return new VirtualMeasurement<>(device, delay, () -> result);
    }

    /**
     * Returns
     *
     * @param delay
     * @param mean
     * @param sigma
     * @return
     */
    public static Measurement<Double> randomDoubleMeasurement(Device device, Duration delay, double mean, double sigma) {
        return new VirtualMeasurement<>(device, delay, () -> generator.nextGaussian() * sigma + mean);
    }

//    public static <T> Sensor<T> virtualSensor(Function<Meta, Measurement<T>> factory, StateDef... states) {
//        return new Sensor<T>() {
//            @Override
//            protected Measurement<T> createMeasurement() {
//                return factory.apply(meta());
//            }
//
//            @Override
//            public String type() {
//                return VIRTUAL_SENSOR_TYPE;
//            }
//
//            @Override
//            public List<StateDef> stateDefs() {
//                return Arrays.asList(states);
//            }
//        };
//    }
//
//    public static <T> Sensor<T> virtualSensor(Function<Meta, Measurement<T>> factory) {
//        return new Sensor<T>() {
//            @Override
//            protected Measurement<T> createMeasurement() {
//                return factory.apply(meta());
//            }
//
//            @Override
//            public String type() {
//                return VIRTUAL_SENSOR_TYPE;
//            }
//        };
//    }

    public static Sensor<Double> randomDoubleSensor(Context context, String name, Duration delay, double mean, double sigma) {
        Sensor<Double> sensor = new Sensor<Double>() {
            {
                setContext(context);
                setMeta(new MetaBuilder("device").setValue("name", name).build());
            }

            @Override
            protected Measurement<Double> createMeasurement() {
                return randomDoubleMeasurement(this, delay, mean, sigma);
            }

            @Override
            public String getType() {
                return VIRTUAL_SENSOR_TYPE;
            }
        };

        try {
            sensor.init();
        } catch (ControlException ex) {
            throw new Error(ex);
        }
        return sensor;
    }

    /**
     * Return a sensor producing normally distributed doubles with given mean and sigma
     *
     * @param context
     * @param meta
     * @return
     */
    public static Sensor<Double> randomDoubleSensor(Context context, Meta meta) {
        return randomDoubleSensor(
                context,
                meta.getString("name", "sensor"),
                Duration.parse(meta.getString("delay", "PT0.2S")),
                meta.getDouble("mean", 1.0),
                meta.getDouble("sigma", 0.1)
        );
    }

}
