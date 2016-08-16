/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.computation.GoalGroup;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.navigation.Provider;
import hep.dataforge.utils.GenericBuilder;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A universal data container
 *
 * @author Alexander Nozik
 */
public interface DataNode<T> extends Iterable<NamedData<? extends T>>, Named, Annotated, Provider {

    String DATA_TARGET = "data";
    String NODE_TARGET = "node";
    String DEFAULT_DATA_FRAGMENT_NAME = "";

    static <T> DataNode<T> empty(String name, Class<T> type) {
        return new EmptyDataNode<>(name, type);
    }

    static DataNode empty() {
        return new EmptyDataNode<>("", Object.class);
    }

    /**
     * A data node wrapping single data
     *
     * @param <T>
     * @param dataName
     * @param data
     * @param nodeMeta
     * @return
     */
    static <T> DataNode<T> of(String dataName, Data<T> data, Meta nodeMeta) {
        return DataSet.builder(data.type())
                .setName(dataName)
                .setMeta(nodeMeta)
                .putData(dataName, data)
                .build();
    }

    /**
     * Get Data with given Name or null if name not present
     *
     * @param name
     * @return
     */
    Optional<Data<? extends T>> getData(String name);

    default <R> Data<R> getCheckedData(String dataName, Class<R> type) {
        Data<? extends T> data = getData(dataName).orElseThrow(() -> new NameNotFoundException(dataName));
        if (type.isAssignableFrom(data.type())) {
            return (Data<R>) data;
        } else {
            throw new RuntimeException(String.format("Type check failed: expected %s but found %s", type.getName(), data.type().getName()));
        }
    }

    /**
     * Compute specific Data. Blocking operation
     *
     * @param name
     * @return
     */
    default T compute(String name) {
        return getData(name).orElseThrow(() -> new NameNotFoundException(name)).get();
    }

    /**
     * Get default data fragment. Access first data element in this node if it
     * is not present. Useful for single data nodes.
     *
     * @return
     */
    default Data<? extends T> getData() {
        return getData(DEFAULT_DATA_FRAGMENT_NAME)
                .orElse(dataStream().findFirst().orElseThrow(() -> new RuntimeException("Data node is empty")));
    }

    /**
     * Get descendant node in case of tree structure. In case of flat structure
     * returns node composed of all Data elements with names that begin with
     * {@code <nodename>.}. Child node inherits meta from parent node. In case
     * both nodes have meta, it is merged.
     *
     * @param nodeName
     * @return
     */
    Optional<DataNode<? extends T>> getNode(String nodeName);

    /**
     * Get the node assuming it have specific type with type check
     *
     * @param nodeName
     * @param type
     * @param <R>
     * @return
     */
    default <R> DataNode<R> getCheckedNode(String nodeName, Class<R> type) {
        DataNode<? extends T> node;
        if (nodeName.isEmpty()) {
            node = this;
        } else {
            node = getNode(nodeName).orElseThrow(() -> new NameNotFoundException(nodeName));
        }
        if (type.isAssignableFrom(node.type())) {
            return (DataNode<R>) node;
        } else {
            throw new RuntimeException(String.format("Type check failed: expected %s but found %s", type.getName(), node.type().getName()));
        }
    }

    /**
     * Named dataStream of data elements including subnodes if they are present.
     * Meta of each data is supposed to be laminate containing node meta.
     *
     * @return
     */
    Stream<NamedData<? extends T>> dataStream();

    /**
     * Iterate other all data pieces using given predicate
     *
     * @param consumer
     */
    default void forEachData(Predicate<NamedData> predicate, Consumer<NamedData<? extends T>> consumer) {
        dataStream().filter(predicate).forEach(d -> consumer.accept(d));
    }

    /**
     * Iterate other all data pieces with given type with type check
     *
     * @param type
     * @param consumer
     */
    default <R> void forEachDataWithType(Class<R> type, Consumer<NamedData<R>> consumer) {
        dataStream().filter(d -> type.isAssignableFrom(d.type()))
                .forEach(d -> consumer.accept((NamedData<R>) d));
    }

    /**
     * Named recursive node stream. Each node has composite name and Laminate meta including all higher nodes information
     *
     * @return
     */
    Stream<DataNode<? extends T>> nodeStream();

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
     * Force start data computation for all data and wait for completion
     */
    default DataNode<T> computeAll() throws Exception {
        nodeGoal().get();
        return this;
    }

    /**
     * Computation control for data
     *
     * @return
     */
    default GoalGroup nodeGoal() {
        return new GoalGroup(this.dataStream()
                .map(entry -> entry.getGoal()).collect(Collectors.toList()));
    }

    default void onComplete(Consumer<DataNode<T>> consumer) {
        nodeGoal().onComplete((res, err) -> consumer.accept(DataNode.this));
    }

    @Override
    default Iterator<NamedData<? extends T>> iterator() {
        return dataStream().iterator();
    }

    public interface Builder<T, N extends DataNode<T>, B extends Builder> extends GenericBuilder<N, B> {

        Class<T> type();

        B setName(String name);

        B setMeta(Meta meta);

        B putData(String key, Data<? extends T> data, boolean replace);

        default B putData(String key, Data<? extends T> data) {
            return putData(key, data, false);
        }

        B putNode(String as, DataNode<? extends T> node);

        B removeNode(String nodeName);

        B removeData(String dataName);

        default B putNode(DataNode<? extends T> node) {
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
            return putData(NamedData.buildStatic(key, staticData, Meta.empty()));
        }
    }

}
