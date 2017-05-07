/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices.annotations;

import java.lang.annotation.*;

/**
 * The role of connection served by this device
 * @author Alexander Nozik
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(RoleDefs.class)
public @interface RoleDef {
    /**
     * Role name
     * @return 
     */
    String name();
    
    /**
     * Role description
     * @return 
     */
    String info() default "";
    
    /**
     * The type of the object that could play this role
     * @return 
     */
    Class objectType() default Object.class;
}
