/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind

import hep.dataforge.values.Value
import spock.lang.Specification

/**
 *
 * @author Alexander Nozik
 */
class ValueExtensionSpec extends Specification{
    def "value plus operation"(){
        when:
            Value a = Value.of("123");
            def b = 4;
        then:
            a + b == Value.of(127)
    }
    
    
}

