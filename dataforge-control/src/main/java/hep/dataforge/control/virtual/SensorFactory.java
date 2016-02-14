/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.virtual;

import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.control.devices.annotations.RoleDef;
import hep.dataforge.control.devices.annotations.StateDef;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.control.measurements.Sensor;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A factory for virtual sensors
 *
 * @author Alexander Nozik
 */
public class SensorFactory<T> {

    private Function<Sensor<T>, T> valueFactory;
    private Function<Sensor<T>, Duration> delayFactory = (sensor) -> Duration.ZERO;
    private String name;
    private Context context = GlobalContext.instance();
    private Meta meta = Meta.buildEmpty("device");
    private List<StateDef> states = new ArrayList<>();
    private List<RoleDef> roles = new ArrayList<>();

    public SensorFactory(String name, Function<Sensor<T>, T> valueFactory) {
        this.valueFactory = valueFactory;
        this.name = name;
    }

    public SensorFactory<T> setValueFactory(Function<Sensor<T>, T> valueFactory) {
        this.valueFactory = valueFactory;
        return this;
    }

    public SensorFactory<T> setDelayFactory(Function<Sensor<T>, Duration> delayFactory) {
        this.delayFactory = delayFactory;
        return this;
    }

    public SensorFactory<T> setName(String name) {
        this.name = name;
        return this;
    }

    public SensorFactory<T> setContext(Context context) {
        this.context = context;
        return this;
    }

    public SensorFactory<T> setMeta(Meta meta) {
        this.meta = meta;
        return this;
    }

    public SensorFactory<T> setStates(List<StateDef> states) {
        this.states = states;
        return this;
    }

    public SensorFactory<T> setRoles(List<RoleDef> roles) {
        this.roles = roles;
        return this;
    }

    //TODO add methods for custom states
    public SensorFactory<T> addState(String name) {
        states.add(new StateDef() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String info() {
                return "";
            }

            @Override
            public boolean readOnly() {
                return false;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return StateDef.class;
            }
        });
        return this;
    }

    public SensorFactory<T> addRole(String name) {
        roles.add(new RoleDef() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String info() {
                return "";
            }

            @Override
            public Class objectType() {
                return Object.class;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RoleDef.class;
            }
        });
        return this;
    }

    public Sensor<T> build() {
        Sensor<T> sensor = new Sensor<T>(name, context, meta) {
            @Override
            protected Measurement<T> createMeasurement() throws MeasurementException {
                return new VirtualMeasurement<>(delayFactory.apply(this), () -> valueFactory.apply(this));
            }

            @Override
            public List<RoleDef> roleDefs() {
                return roles;
            }

            @Override
            public List<StateDef> stateDefs() {
                return states;
            }

            @Override
            protected boolean applyState(String stateName, Value stateValue) throws ControlException {
                return hasState(stateName);
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
