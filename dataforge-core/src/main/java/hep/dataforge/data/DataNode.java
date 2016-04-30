/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.names.Named;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.navigation.Provider;
import hep.dataforge.utils.GenericBuilder;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
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
    public static final String DEFAULT_DATA_FRAGMENT_NAME = "";

    public static <T> DataNode<T> empty(String name, Class<T> type) {
        return new EmptyDataNode<>(name, type);
    }

    public static DataNode empty() {
        return new EmptyDataNode<>("", Object.class);
    }

    /**
     * Get Data with given Name or null if name not present
     *
     * @param name
     * @return
     */
    Data<? extends T> getData(String name);

    /**
     * Get default data fragment. Access first data element in this node if it
     * is not present. Useful for single data nodes.
     *
     * @return
     */
    default Data<? extends T> getData() {
        Data<? extends T> res = getData(DEFAULT_DATA_FRAGMENT_NAME);
        if (res != null) {
            return res;
        } else {
            return dataStream().findFirst().orElse(null).getValue();
        }
    }

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

    /**
     * Named dataStream of data elements including subnodes if they are present
     *
     * @return
     */
    Stream<Pair<String, Data<? extends T>>> dataStream();

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
     * Force start data computation for all data
     */
    default DataNode<T> compute() {
        computation().join();
        return this;
    }

    /**
     * Cancel all data computation
     */
    default void cancel() {
        computation().cancel(true);
    }

    /**
     * Computation control for data
     *
     * @return
     */
    default CompletableFuture<Void> computation() {
        CompletableFuture<?>[] futures = this.dataStream()
                .<CompletableFuture>map(item -> item.getValue().getInFuture())
                .toArray((int value) -> new CompletableFuture[value]);
        return CompletableFuture.allOf(futures);
    }

    public interface Builder<T, N extends DataNode<T>, B extends Builder> extends GenericBuilder<N, B> {

        Class<T> type();

        B setName(String name);

        B setMeta(Meta meta);

        B putData(String key, Data<? extends T> data);
        
        B putNode(String as, DataNode<? extends T> node);
        
        default B putNode(DataNode<? extends T> node){
            return putNode(node.getName(), node);
        }

        default B putData(NamedData<? extends T> data) {
            return putData(data.getName(), data);
        }

        default B putAll(Collection<NamedData<? extends T>> dataCollection) {
            dataCollection.stream().forEach(it -> putData(it));
            return self();
        }

        default B putAll(Map<String, Data<? extends T>> map) {
            map.forEach(this::putData);
            return self();
        }

        default B putStatic(String key, T staticData) {
            if (!type().isInstance(staticData)) {
                throw new IllegalArgumentException("The data mast be instance of " + type().getName());
            }
            return putData(new NamedStaticData<>(key, staticData, type()));
        }
    }

}
