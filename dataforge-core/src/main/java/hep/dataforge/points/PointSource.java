/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.points;

/**
 *
 * @author Alexander Nozik
 */
public interface PointSource extends Iterable<DataPoint> {

    PointFormat getFormat();
    
}
