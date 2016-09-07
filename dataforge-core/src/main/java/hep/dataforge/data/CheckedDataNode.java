package hep.dataforge.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.navigation.Path;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A wrapper for DataNode that allowes to access speciffically typed content.
 * Created by darksnake on 07-Sep-16.
 */
public class CheckedDataNode<T> implements DataNode<T> {
    private final DataNode<?> node;
    private final Class<T> type;

    public CheckedDataNode(DataNode<?> node, Class<T> type) {
        this.node = node;
        this.type = type;
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
    public Optional<Data<? extends T>> getData(String name) {
        return node.getData(name).flatMap(d -> {
            if (type.isAssignableFrom(d.type())) {
                return Optional.of((Data<? extends T>) d);
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<DataNode<? extends T>> getNode(String nodeName) {
        return node.getNode(nodeName).flatMap(n -> {
            if (type.isAssignableFrom(n.type())) {
                return Optional.of((DataNode<? extends T>) n);
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Stream<NamedData<? extends T>> dataStream() {
        return node.dataStream().filter(d -> type.isAssignableFrom(d.type())).map(d -> (NamedData<? extends T>) d);
    }

    @Override
    public Stream<DataNode<? extends T>> nodeStream() {
        return node.nodeStream().filter(n -> type.isAssignableFrom(n.type())).map(n -> (DataNode<? extends T>) n);
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Object provide(Path path) {
        return node.provide(path);
    }

    @Override
    public boolean provides(Path path) {
        return node.provides(path);
    }
}
