/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.tables;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Alexander Nozik
 */
public interface PointSource extends Iterable<DataPoint> {

    TableFormat getFormat();

    default Stream<DataPoint> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
    
}
