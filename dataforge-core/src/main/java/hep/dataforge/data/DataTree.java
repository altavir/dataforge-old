/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.navigation.AbstractProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * A tree data structure
 *
 * @author Alexander Nozik
 */
public class DataTree<T> extends AbstractProvider implements DataNode<T> {

    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder(type);
    }

    /**
     * Create an empty typed DataTree with given Name. If name is composite,
     * than the whole tree structure is created
     *
     * @param <T>
     * @param name
     * @return
     */
    public static <T> DataTree<T> emptyTree(Class<T> type, Name name) {
        if (name.length() == 1) {
            DataTree<T> res = new DataTree<>(type);
            res.name = name.toString();
            return res;
        } else {
            DataTree<T> res = new DataTree<>(type);
            res.name = name.getFirst().toString();
            Name tail = name.cutFirst();
            res.nodes.put(tail.getFirst().toString(), emptyTree(type, tail));
            return res;
        }
    }

    private String name = "";
    private Meta meta = Meta.empty();
    private final Class<T> type;

    private DataTree<? super T> parent;
    private final Map<String, DataTree<T>> nodes = new HashMap<>();
    private final Map<String, Data<? extends T>> data = new HashMap<>();

    private DataTree(Class<T> type) {
        this.type = type;
    }

    /**
     * Shallow copy-constructor
     *
     * @param tree
     */
    private DataTree(DataTree<T> tree) {
        this.type = tree.type;
        this.parent = tree.parent;
        this.meta = tree.meta;
        this.name = tree.name;
        this.nodes.putAll(tree.nodes);
        this.data.putAll(tree.data);
    }

    /**
     * Parent of this node.
     *
     * @return
     */
    public DataTree<? super T> parent() {
        return parent;
    }

    public Collection<String> nodeNames() {
        return nodes.keySet();
    }

    @Override
    public Data<? extends T> getData(String dataName) {
        return getData(Name.of(dataName));
    }

    public Data<? extends T> getData(Name dataName) {
        if (dataName.length() == 1) {
            return data.get(dataName.toString());
        } else {
            DataNode<? extends T> node = getNode(dataName.cutLast());
            if (node != null) {
                return node.getData(dataName.getLast().toString());
            } else {
                return null;
            }
        }
    }

    /**
     * Private method to add data to the node
     *
     * @param name
     * @param data
     */
    private void putData(Name name, Data<? extends T> data) {
        if (name.length() == 1) {
            String key = name.toString();
            if (type.isInstance(data.dataType())) {
                if (!this.data.containsKey(key)) {
                    this.data.put(key, data);
                } else {
                    throw new RuntimeException("The data with key " + key + " already exists");
                }
            } else {
                throw new RuntimeException("Data does not satisfy class boundary");
            }
        } else {
            String head = name.getFirst().toString();
            if(this.nodes.containsKey(head)){
                this.nodes.get(head).putData(name.cutFirst(), data);
            } else {
                DataTree<T> newNode = new DataTree<>(type);
                newNode.name = head;
                newNode.putData(name.cutFirst(), data);
                this.nodes.put(head, newNode);
            }
        }
    }

    @Override
    public Stream<Pair<String, Data<? extends T>>> stream() {
        Stream<Pair<String, Data<? extends T>>> dataStream = data.entrySet()
                .stream()
                .<Pair<String, Data<? extends T>>>map((Map.Entry<String, Data<? extends T>> entry)
                        -> new Pair<>(entry.getKey(), entry.getValue()));

        // iterating over nodes including node name into stream
        Stream<Pair<String, Data<? extends T>>> subStream = nodes.entrySet()
                .stream()
                .<Pair<String, Data<? extends T>>>flatMap((Map.Entry<String, DataTree<T>> nodeEntry)
                        -> nodeEntry.getValue().stream()
                        .<Pair<String, Data<? extends T>>>map(it -> new Pair<>(nodeEntry.getKey() + "." + it.getKey(), it.getValue())));

        return Stream.concat(dataStream, subStream);
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty() && nodes.isEmpty();
    }

    @Override
    public int size() {
        return data.size() + nodes.values().stream().mapToInt(DataNode::size).sum();
    }

    @Override
    public DataTree<? extends T> getNode(String nodeName) {
        return getNode(Name.of(nodeName));
    }

    public DataTree<? extends T> getNode(Name nodeName) {
        String child = nodeName.getFirst().toString();
        if (nodeName.length() == 1) {
            return nodes.get(nodeName.toString());
        } else if (this.nodes.containsKey(child)) {
            return this.nodes.get(child).getNode(nodeName.cutFirst().toString());
        } else {
            return null;
        }
    }

    @Override
    public Iterator<Data<? extends T>> iterator() {
        return stream().<Data<? extends T>>map(it -> it.getValue()).iterator();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Meta meta() {
        if (parent() == null) {
            return meta;
        } else {
            return new Laminate(meta, parent().meta());
        }
    }

    @Override
    protected boolean provides(String target, Name name) {
        switch (target) {
            case NODE_TARGET:
                return getNode(name) != null;
            case DATA_TARGET:
                return getData(name) != null;
            default:
                return false;
        }
    }

    @Override
    protected Object provide(String target, Name name) {
        //FIXME replace nulls by unchecked exceptions
        switch (target) {
            case NODE_TARGET:
                return getNode(name);
            case DATA_TARGET:
                return getData(name);
            default:
                return null;
        }
    }

    public static class Builder<T> {

        private final DataTree<T> tree;

        private Builder(Class<T> type) {
            this.tree = new DataTree<>(type);
        }

        public Builder<T> setName(String name) {
            tree.name = name;
            return this;
        }

        public Builder<T> setMeta(Meta meta) {
            if (meta == null) {
                tree.meta = Meta.empty();
            } else {
                tree.meta = meta;
            }
            return this;
        }

        public Builder<T> putBranch(Builder<T> builder) {
            return putBranch(builder.tree);
        }

        public Builder<T> putBranch(DataTree<T> node) {
            if (tree.type.isAssignableFrom(node.type())) {
                DataTree<T> newNode = new DataTree<>(node);
                tree.nodes.put(node.getName(), newNode);

                newNode.parent = tree;
                return this;
            } else {
                throw new RuntimeException("Node type not compatible");
            }
        }

        public Builder<T> putData(Name name, Data<? extends T> data) {
            tree.putData(name, data);
            return this;
        }

        /**
         * Add data
         *
         * @param key
         * @param data
         * @return
         */
        public Builder<T> putData(String key, Data<? extends T> data) {
            return putData(Name.of(key), data);
        }

        public Builder<T> putData(NamedData<? extends T> data) {
            return putData(data.getName(), data);
        }

        public Builder<T> putAll(Collection<NamedData<? extends T>> dataCollection) {
            dataCollection.stream().forEach(it -> putData(it));
            return this;
        }

        public Builder<T> putAll(Map<String, Data<? extends T>> map) {
            tree.data.putAll(map);
            return this;
        }

        public Builder<T> putStatic(String key, T staticData) {
            if (!tree.type.isInstance(staticData)) {
                throw new IllegalArgumentException("The data mast be instance of " + tree.type.getName());
            }
            return putData(new NamedStaticData<>(key, staticData, tree.type));
        }

        public DataTree<T> build() {
            return tree;
        }

    }

}
