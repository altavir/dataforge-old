/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * @author Alexander Nozik
 */
@Target({ElementType.PARAMETER})
public @interface NonNull {
    
}
