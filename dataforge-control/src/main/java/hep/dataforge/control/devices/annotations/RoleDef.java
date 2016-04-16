/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
     * The type of the object that could play the role
     * @return 
     */
    Class objectType() default Object.class;
}
