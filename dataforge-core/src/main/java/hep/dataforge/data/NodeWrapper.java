package hep.dataforge.data;

import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Data node wrapper to add parent name and meta to existing node
 * Created by darksnake on 14-Aug-16.
 */
public class NodeWrapper<T> implements DataNode<T> {
    private final Laminate overrideMeta;
    private final String overrideName;
    private final DataNode<T> node;

    public NodeWrapper(DataNode<T> node, String parentName, Meta parentMeta) {
        if (parentMeta instanceof Laminate) {
            this.overrideMeta = ((Laminate) parentMeta).addFirstLayer(node.meta());
        } else {
            this.overrideMeta = new Laminate(node.meta(), parentMeta);
        }
        this.overrideName = parentName.isEmpty() ? node.getName() : Name.joinString(parentName, node.getName());
        this.node = node;
    }

    @Override
    public Optional<Data<? extends T>> optData(String name) {
        return node.optData(name);
    }

    @Override
    public Optional<DataNode<? extends T>> optNode(String nodeName) {
        return node.optNode(nodeName);
    }

    @Override
    public Stream<NamedData<? extends T>> dataStream(boolean recursive) {
        return node.dataStream(recursive);
    }

    @Override
    public Stream<DataNode<? extends T>> nodeStream(boolean recursive) {
        return node.nodeStream(recursive);
    }

    @Override
    public Class<T> type() {
        return node.type();
    }

    @Override
    public boolean isEmpty() {
        return node.isEmpty();
    }

    @Override
    public Laminate meta() {
        return overrideMeta;
    }

    @Override
    public String getName() {
        return overrideName;
    }
}
