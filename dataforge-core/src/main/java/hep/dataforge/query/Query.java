/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.query;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic interface for any query.
 *
 * @author Alexander Nozik
 */
@Deprecated
public interface Query<T> {

    /**
     * Execute query result and present it as a stream
     * @return 
     */
    Stream<T> makeStream();
    
    /**
     * Execute a query and present its results. It is supposed that query
     * remains valid after execution and could be executed second time.
     *
     * @return
     */
    default Collection<T> make(){
        return makeStream().collect(Collectors.toList());
    }
}
