package hep.dataforge.data;

import hep.dataforge.meta.Meta;

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
    public Optional<Data<? extends T>> optData(String name) {
        return node.optData(name).flatMap(d -> {
            if (type.isAssignableFrom(d.type())) {
                return Optional.of((Data<T>) d);
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<DataNode<? extends T>> optNode(String nodeName) {
        return node.optNode(nodeName).flatMap(n -> {
            if (type.isAssignableFrom(n.type())) {
                return Optional.of((DataNode<T>) n);
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Stream<NamedData<T>> dataStream(boolean recursive) {
        return node.dataStream(recursive).filter(d -> type.isAssignableFrom(d.type())).map(d -> (NamedData<T>) d);
    }

    @Override
    public Stream<DataNode<T>> nodeStream(boolean recursive) {
        return node.nodeStream(recursive).filter(n -> type.isAssignableFrom(n.type())).map(n -> (DataNode<T>) n);
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
