/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.names.Named;
import hep.dataforge.meta.Annotated;
import hep.dataforge.navigation.Provider;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * A universal data container
 *
 * @author Alexander Nozik
 */
public interface DataNode<T> extends Iterable<Data<? extends T>>, Named, Annotated, Provider {

    public static final String DATA_TARGET = "data";
    public static final String NODE_TARGET = "node";

    public static <T> DataNode<T> empty(String name, Class<T> type) {
        return new EmptyDataNode<>(name, type);
    }
    
    public static DataNode empty() {
        return new EmptyDataNode<>("", Object.class);
    }    

    /**
     * Get Data with given Name
     *
     * @param name
     * @return
     */
    Data<? extends T> getData(String name);
    
    /**
     * Access first data element in this node. Useful for single data nodes.
     * @return 
     */
    default Data<? extends T> getData(){
        return stream().findFirst().orElse(null).getValue();
    }

    /**
     * Named stream of data elements including subnodes if they are present
     *
     * @return
     */
    Stream<Pair<String, Data<? extends T>>> stream();

    /**
     * Get border type for this DataNode
     *
     * @return
     */
    Class<T> type();

    /**
     * Shows if there is no data in this node
     *
     * @return
     */
    boolean isEmpty();

    /**
     * The current number of data pieces in this node
     *
     * @return
     */
    int size();

    /**
     * Get descendant node in case of tree structure. In case of flat structure
     * returns node composed of all Data elements with names that begin with
     * {@code <nodename>.}. Child node inherits meta from parent node. In case
     * both nodes have meta, it is merged.
     *
     * @param <N>
     * @param nodeName
     * @return
     */
    DataNode<? extends T> getNode(String nodeName);
}
