/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.dependencies;

import hep.dataforge.content.Named;
import hep.dataforge.meta.Annotated;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * A container for a set of Data
 * @author Alexander Nozik
 */
public interface DataNode<T> extends Iterable<Data<? extends T>>, Named, Annotated{

    /**
     * Get Data with given Name
     * @param name
     * @return 
     */
    Data<? extends T> getData(String name);
    
    /**
     * Named stream of data elements
     * @return 
     */
    Stream<Pair<String, Data<? extends T>>> stream();
    
    /**
     * Get border type for this DataNode
     * @return 
     */
    Class<T> type();
    
    /**
     * Shows if there is no data in this node
     * @return 
     */
    boolean isEmpty();
    
    /**
     * The current number of data pieces in this node
     * @return 
     */
    int size();
}
