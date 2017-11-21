package hep.dataforge.data;

import hep.dataforge.meta.Meta;
import org.slf4j.LoggerFactory;

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
        //TODO add warning for incompatible types
        if(isEmpty()){
            LoggerFactory.getLogger(getClass()).warn("The checked node is empty");
        }
    }

    @Override
    public Meta getMeta() {
        return node.getMeta();
    }

    @Override
    public String getName() {
        return node.getName();
    }

    @Override
    public Optional<Data<T>> optData(String name) {
        return node.optData(name).flatMap(d -> {
            if (type.isAssignableFrom(d.type())) {
                return Optional.of(d.cast(type));
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<DataNode<T>> optNode(String nodeName) {
        return node.optNode(nodeName).flatMap(n -> {
            if (type.isAssignableFrom(n.type())) {
                return Optional.of(n.checked(type));
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Stream<NamedData<? extends T>> dataStream(boolean recursive) {
        return node.dataStream(recursive).filter(d -> type.isAssignableFrom(d.type())).map(d -> d.cast(type));
    }

    @Override
    public Stream<DataNode<? extends T>> nodeStream(boolean recursive) {
        return node.nodeStream(recursive).filter(n -> type.isAssignableFrom(n.type())).map(n -> n.checked(type));
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        return dataStream(true).count() == 0;
    }
}
