/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.virtual;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A factory for virtual sensors
 *
 * @author Alexander Nozik
 */
public class VirtualSensorFactory<T> {

    private Function<Sensor<T>, T> valueFactory = (Sensor<T> sensor) -> null;
    private Function<Sensor<T>, Duration> delayFactory = (sensor) -> Duration.ZERO;
    private String name = "device";
    private Context context = Global.instance();
    private Meta meta = Meta.buildEmpty("device");
    private List<StateDef> states = new ArrayList<>();
    private List<RoleDef> roles = new ArrayList<>();
    private final Map<String, BiConsumer<Sensor<T>, Value>> commands = new HashMap<>();

    public VirtualSensorFactory<T> setValueFactory(Function<Sensor<T>, T> valueFactory) {
        this.valueFactory = valueFactory;
        return this;
    }

    public VirtualSensorFactory<T> setDelayFactory(Function<Sensor<T>, Duration> delayFactory) {
        this.delayFactory = delayFactory;
        return this;
    }

    public VirtualSensorFactory<T> setName(String name) {
        this.name = name;
        return this;
    }

    public VirtualSensorFactory<T> setContext(Context context) {
        this.context = context;
        return this;
    }

    public VirtualSensorFactory<T> setMeta(Meta meta) {
        this.meta = meta;
        return this;
    }

    public VirtualSensorFactory<T> setStates(List<StateDef> states) {
        this.states = states;
        return this;
    }

    public VirtualSensorFactory<T> setRoles(List<RoleDef> roles) {
        this.roles = roles;
        return this;
    }

    public VirtualSensorFactory<T> addCommand(String commandName, BiConsumer<Sensor<T>, Value> consumer) {
        commands.put(commandName, consumer);
        return this;
    }

    //TODO add methods for custom states
    public VirtualSensorFactory<T> addState(String name) {
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
            public Class<? extends Annotation> annotationType() {
                return StateDef.class;
            }
        });
        return this;
    }

    public VirtualSensorFactory<T> addRole(String name) {
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
        Sensor<T> sensor = new Sensor<T>() {
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
            public void command(String commandName, Value argument) throws ControlException {
                if (commands.containsKey(commandName)) {
                    commands.get(commandName).accept(this, argument);
                } else {
                    super.command(commandName, argument);
                }
            }

        };

        sensor.setName(name);
        sensor.setMeta(meta);
        sensor.setContext(context);
        try {
            sensor.init();
        } catch (ControlException ex) {
            throw new Error(ex);
        }
        return sensor;
    }

}
