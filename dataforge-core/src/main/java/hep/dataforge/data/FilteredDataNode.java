package hep.dataforge.data;

import hep.dataforge.meta.Meta;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Filtered node does not change structure of underlying node, just filters output
 *
 * @param <T>
 */
public class FilteredDataNode<T> implements DataNode<T> {
    private final DataNode<T> node;
    private final BiPredicate<String, Data<T>> predicate;

    public FilteredDataNode(DataNode<T> node, BiPredicate<String, Data<T>> predicate) {
        this.node = node;
        this.predicate = predicate;
    }

    @Override
    public Meta meta() {
        return node.meta();
    }

    @Override
    public String getName() {
        return node.getName();
    }

    @Override
    public Optional<Data<T>> optData(String name) {
        return node.optData(name).flatMap(d -> {
            if (predicate.test(name, d)) {
                return Optional.of(d);
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<DataNode<T>> optNode(String nodeName) {
        return node.optNode(nodeName).map(it -> new FilteredDataNode<>(it, predicate));
    }

    @Override
    public Stream<NamedData<? extends T>> dataStream(boolean recursive) {
        return node.dataStream(recursive).filter(d -> predicate.test(d.getName(), d.cast(type())));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<DataNode<? extends T>> nodeStream(boolean recursive) {
        return node.nodeStream(recursive).map(n -> n.filter((name, data) -> predicate.test(name, (Data<T>)data)));
    }

    @Override
    public Class<T> type() {
        return node.type();
    }

    @Override
    public boolean isEmpty() {
        return dataStream(true).count() == 0;
    }
}
