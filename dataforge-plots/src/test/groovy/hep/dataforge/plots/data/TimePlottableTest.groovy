/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.plots.data

import java.time.Instant
import spock.lang.Specification
import hep.dataforge.values.Value
import java.time.Duration

/**
 *
 * @author darksnake
 */
class TimePlottableTest extends Specification {

    def "test maxItems"(){
        when:
        TimePlottable pl = new TimePlottable("test",null);
        pl.setMaxItems(50);
        Instant time = Instant.now();
        for(int i = 0; i<60; i++){
            time = time.plusMillis(10);
            pl.put(time,Value.of(time.toEpochMilli()));
        }
        then:
        pl.size() == 50;
    }
    
    def "test maxAge"(){
        when:
        TimePlottable pl = new TimePlottable("test",null);
        pl.setMaxAge(Duration.ofMillis(500));
        Instant time = Instant.now();
        for(int i = 0; i<60; i++){
            time = time.plusMillis(10);
            pl.put(time,Value.of(time.toEpochMilli()));
        }
        then:
        pl.size() == 52;
    }    
	
}

