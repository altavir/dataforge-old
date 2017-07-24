/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.tables;

import hep.dataforge.values.Values;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A finite or infinite source of DataPoints
 * @author Alexander Nozik
 */
public interface PointSource extends Iterable<Values> {


//    TableFormat getFormat();

    default Stream<Values> getRows() {
        return StreamSupport.stream(this.spliterator(), false);
    }
    
}
