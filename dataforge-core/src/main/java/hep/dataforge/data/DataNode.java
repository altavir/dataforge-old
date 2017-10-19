/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.goals.GoalGroup;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;
import hep.dataforge.utils.GenericBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A universal data container
 *
 * @author Alexander Nozik
 */
public interface DataNode<T> extends Iterable<NamedData<T>>, Named, Metoid, Provider {

    String DATA_TARGET = "data";
    String NODE_TARGET = "node";
    String DEFAULT_DATA_FRAGMENT_NAME = "@default";

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
    @Provides(DATA_TARGET)
    Optional<Data<T>> optData(String name);

    @SuppressWarnings("unchecked")
    default <R> Data<R> getCheckedData(String dataName, Class<R> type) {
        Data<? extends T> data = optData(dataName).orElseThrow(() -> new NameNotFoundException(dataName));
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
        return optData(name).orElseThrow(() -> new NameNotFoundException(name)).get();
    }

    /**
     * Get default data fragment. Access first data element in this node if it
     * is not present. Useful for single data nodes.
     *
     * @return
     */
    default Data<T> getData() {
        return optData(DEFAULT_DATA_FRAGMENT_NAME)
                .orElse(dataStream(true).findFirst().map(it -> it.cast(type()))
                        .orElseThrow(() -> new RuntimeException("Data node is empty"))
                );
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
    @Provides(NODE_TARGET)
    Optional<DataNode<T>> optNode(String nodeName);

    default DataNode<T> getNode(String nodeName) {
        return optNode(nodeName).get();
    }

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
            node = optNode(nodeName).orElseThrow(() -> new NameNotFoundException(nodeName));
        }

        return node.checked(type);
    }

    /**
     * Named dataStream of data elements including subnodes if they are present.
     * Meta of each data is supposed to be laminate containing node meta.
     *
     * @return
     */
    Stream<NamedData<? extends T>> dataStream(boolean recursive);

    default Stream<NamedData<? extends T>> dataStream() {
        return dataStream(true);
    }

    /**
     * Iterate other all data pieces with given type with type check
     *
     * @param type
     * @param consumer
     */
    default <R> void forEachData(Class<R> type, Consumer<NamedData<R>> consumer) {
        dataStream().filter(d -> type.isAssignableFrom(d.type()))
                .forEach(d -> consumer.accept(d.cast(type)));
    }

    /**
     * A stream of subnodes. Each node has composite name and Laminate meta including all higher nodes information
     *
     * @param recursive if true then recursive node stream is returned, otherwise only upper level children are used
     * @return
     */
    Stream<DataNode<? extends T>> nodeStream(boolean recursive);

    /**
     * A recursive node stream
     *
     * @return
     */
    default Stream<DataNode<? extends T>> nodeStream() {
        return nodeStream(true);
    }

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
     * The current number of data pieces in this node including subnodes
     *
     * @return
     */
    default long dataSize(boolean recursive) {
        return dataStream(recursive).count();
    }

    default long dataSize() {
        return dataSize(true);
    }

    default long nodesSize(boolean recursive) {
        return nodeStream(recursive).count();
    }

    default long nodesSize() {
        return nodesSize(true);
    }

    /**
     * Force start data goals for all data and wait for completion
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
                .map(Data::getGoal).collect(Collectors.toList()));
    }

    /**
     * Handle result when the node is evaluated. Does not trigger node evaluation. Ignores exceptional completion
     *
     * @param consumer
     */
    default void handle(Consumer<DataNode<? super T>> consumer) {
        nodeGoal().onComplete((res, err) -> consumer.accept(DataNode.this));
    }

    /**
     * Same as above but with custom executor
     *
     * @param executor
     * @param consumer
     */
    default void handle(Executor executor, Consumer<DataNode<? super T>> consumer) {
        nodeGoal().onComplete(executor, (res, err) -> consumer.accept(DataNode.this));
    }

    /**
     * Return a type checked node containing this one
     *
     * @param checkType
     * @param <R>
     * @return
     */
    @SuppressWarnings("unchecked")
    default <R> DataNode<R> checked(Class<R> checkType) {
        if (checkType.isAssignableFrom(this.type())) {
            return (DataNode<R>) this;
        } else {
            return new CheckedDataNode<>(this, checkType);
        }
    }

    default DataNode<T> filter(BiPredicate<String, Data<T>> predicate) {
        return new FilteredDataNode<>(this, predicate);
    }

//    default Collection<Data<T>> find(String query){
//        if(query.contains(""))
//    }

    @NotNull
    @Override
    default Iterator<NamedData<T>> iterator() {
        return dataStream().map(it -> it.cast(type())).iterator();
    }

    interface Builder<T, N extends DataNode<T>, B extends Builder> extends GenericBuilder<N, B>, Metoid {

        Class<T> type();

        B setName(String name);

        B setMeta(Meta meta);

        B putData(String key, Data<? extends T> data, boolean replace);

        default B putData(String key, Data<? extends T> data) {
            return putData(key, data, false);
        }

        default B putData(String key, T data, Meta meta) {
            return putData(key, Data.buildStatic(data, meta));
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
            dataCollection.forEach(this::putData);
            return self();
        }

        default B putAll(Map<String, Data<? extends T>> map) {
            map.forEach(this::putData);
            return self();
        }

        default B putStatic(String key, T staticData, Meta meta) {
            if (!type().isInstance(staticData)) {
                throw new IllegalArgumentException("The data mast be instance of " + type().getName());
            }
            return putData(NamedData.buildStatic(key, staticData, meta));
        }

        default B putStatic(String key, T staticData) {
            return putStatic(key, staticData, Meta.empty());
        }
    }

}
