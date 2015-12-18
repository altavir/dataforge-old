/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind.gmaths

import hep.dataforge.maths.Interpolation

/**
 * Some mathematical utilities to work easily from Groovy
 * @author Alexander Nozik
 */
class GMathUtils{
    static def interpolate(Map values, String type = "LINE", lo = null, up = null){
        return Interpolation.interpolate(values, type, lo, up);
    }
    
    static def interpolate(List<Number> xs, List<Number> ys, String type = "LINE", lo = null, up = null){
        Map<Number, Number> values = new HashMap<>();
        for(int i = 0; i < xs.size(); i++){
            values.put(xs[i],ys[i]);
        }
        return Interpolation.interpolate(values, type, lo, up);
    }
}

