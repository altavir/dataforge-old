/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.description.ValueDef;

import java.lang.annotation.*;

/**
 * The definition of state for device.
 * <p>
 * Consider extending it to any state holder
 * </p>
 *
 * @author Alexander Nozik
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(StateDefs.class)
public @interface StateDef {

    /**
     * The definition for state value
     * @return
     */
    ValueDef value();

    /**
     * This state could be read
     *
     * @return
     */
    boolean readable() default true;

    /**
     * This state could be written
     *
     * @return
     */
    boolean writable() default false;
}