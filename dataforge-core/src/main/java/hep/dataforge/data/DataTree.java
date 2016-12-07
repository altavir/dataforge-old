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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A tree data structure
 *
 * @author Alexander Nozik
 */
public class DataTree<T> extends AbstractProvider implements DataNode<T> {

    private final Class<T> type;
    private final Map<String, DataTree<? extends T>> nodes = new HashMap<>();
    private final Map<String, Data<T>> data = new HashMap<>();
    private String name = "";
    private Meta meta = Meta.empty();
    private DataTree<? super T> parent;

    private DataTree(Class<T> type) {
        this.type = type;
    }

    /**
     * Shallow copy-constructor
     *
     * @param tree
     */
    private DataTree(DataTree<T> tree, String as) {
        this.type = tree.type;
//        this.parent = tree.parent;
        this.meta = tree.meta;
        this.name = as;
        tree.nodes.forEach((String key, DataTree<? extends T> tree1) -> {
            nodes.put(key, new DataTree<>(tree1, key));
        });
        this.data.putAll(tree.data);
    }

    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder(type);
    }

    /**
     * A general non-restricting tree builder
     *
     * @return
     */
    public static Builder<?> builder() {
        return new Builder<>(Object.class);
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

    /**
     * A conversion of node to DataTree including deep copy
     *
     * @param <T>
     * @param node
     * @return
     */
    public static <T> DataTree<T> cloneNode(DataNode<T> node, String as) {
        if (node instanceof DataTree) {
            return new DataTree<>((DataTree<T>) node, as);
        } else {
            DataTree.Builder<T> builder = DataTree.builder(node.type());
            builder.setName(as);
            builder.setMeta(node.meta());
            node.dataStream().forEach(d -> {
                builder.putData(d);
                //FIXME use internal node meta
            });
            return builder.build();
        }
    }

    /**
     * Parent of this node.
     *
     * @return
     */
    public DataTree<? super T> parent() {
        return parent;
    }

    private void setParent(DataTree<? super T> parent) {
        this.parent = parent;
    }

    public Collection<String> nodeNames() {
        return nodes.keySet();
    }

    @Override
    public Optional<Data<? extends T>> getData(String dataName) {
        return Optional.ofNullable(getData(Name.of(dataName)));
    }

    protected Data<? extends T> getData(Name dataName) {
        if (dataName.length() == 1) {
            return data.get(dataName.toString());
        } else {
            DataNode<? extends T> node = getNode(dataName.cutLast());
            if (node != null) {
                return node.getData(dataName.getLast().toString()).get();
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
    protected void putData(Name name, Data<? extends T> data, boolean replace) {
        if (name.length() == 1) {
            String key = name.toString();
            checkedPutData(key, data, replace);
        } else {
            String head = name.getFirst().toString();
            if (this.nodes.containsKey(head)) {
                getNode(name.getFirst()).putData(name.cutFirst(), data, replace);
            } else {
                DataTree<T> newNode = new DataTree<>(type);
                newNode.name = head;
                newNode.putData(name.cutFirst(), data, replace);
                this.nodes.put(head, newNode);
            }
        }
    }

    protected void putNode(Name name, DataNode<? extends T> node) {
        if (name.length() == 1) {
            String key = name.toString();
            checkedPutNode(key, node);
        } else {
            String head = name.getFirst().toString();
            if (this.nodes.containsKey(head)) {
                this.nodes.get(head).checkedPutNode(name.cutFirst().toString(), node);
            } else {
                DataTree<T> newNode = new DataTree<>(type);
                newNode.name = head;
                newNode.putNode(name.cutFirst(), node);
                this.nodes.put(head, newNode);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void checkedPutNode(String key, DataNode node) {
        if (type().isInstance(node.type())) {
            if (!this.nodes.containsKey(key)) {
                this.nodes.put(key, cloneNode(node, key));
            } else {
                throw new RuntimeException("The node with key " + key + " already exists");
            }
        } else {
            throw new RuntimeException("Node does not satisfy class boundary");
        }
    }

    /**
     * Type checked put data method. Throws exception if types are not
     * compatible
     *
     * @param key
     * @param data
     */
    @SuppressWarnings("unchecked")
    protected void checkedPutData(String key, Data data, boolean allowReplace) {
        if (type().isAssignableFrom(data.type())) {
            if (!this.data.containsKey(key) || allowReplace) {
                this.data.put(key, data);
            } else {
                throw new RuntimeException("The data with key " + key + " already exists");
            }
        } else {
            throw new RuntimeException("Data does not satisfy class boundary");
        }
    }

    @Override
    public Stream<DataNode<? extends T>> nodeStream(boolean recursive) {
        if (recursive) {
            return nodeStream(Name.EMPTY, new Laminate(meta()));
        } else {
            return nodes.values().stream().map(it -> it);
        }
    }

    private Stream<DataNode<? extends T>> nodeStream(Name parentName, Laminate parentMeta) {
        return nodes.entrySet().stream().flatMap((Map.Entry<String, DataTree<? extends T>> nodeEntry) -> {
            Stream<DataNode<? extends T>> nodeItself = Stream.of(new NodeWrapper<>(nodeEntry.getValue(), parentName.toString(), parentMeta));

            Name childName = parentName.append(nodeEntry.getKey());
            Laminate childMeta = parentMeta.addFirstLayer(nodeEntry.getValue().meta());
            Stream<DataNode<? extends T>> childStream = nodeEntry.getValue().nodeStream(childName, childMeta).map(n -> n);

            return Stream.concat(nodeItself, childStream);
        });
    }


    @Override
    public Stream<NamedData<? extends T>> dataStream(boolean recursive) {
        return dataStream(null, new Laminate(meta()), recursive);
    }

    private Stream<NamedData<? extends T>> dataStream(Name nodeName, Laminate nodeMeta, boolean recursive) {
        Stream<NamedData<? extends T>> dataStream = data.entrySet()
                .stream()
                .map((Map.Entry<String, Data<T>> entry) -> {
                            Name dataName = nodeName == null ? Name.of(entry.getKey()) : nodeName.append(entry.getKey());
                            return NamedData.wrap(dataName, entry.getValue(), nodeMeta);
                        }
                );

        if (!recursive) {
            return dataStream;
        } else {

            // iterating over nodes including node name into dataStream
            Stream<NamedData<? extends T>> subStream = nodes.entrySet()
                    .stream()
                    .flatMap(nodeEntry -> {
                        Name subNodeName = nodeName == null ? Name.of(nodeEntry.getKey()) : nodeName.append(nodeEntry.getKey());
                        return nodeEntry.getValue()
                                .dataStream(subNodeName, nodeMeta.addFirstLayer(nodeEntry.getValue().meta()), true);
                    });
            return Stream.concat(dataStream, subStream);
        }
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
    public Optional<DataNode<? extends T>> getNode(String nodeName) {
        return Optional.ofNullable(getNode(Name.of(nodeName)));
    }

    protected DataTree<T> getNode(Name nodeName) {
        String child = nodeName.getFirst().toString();
        if (nodeName.length() == 1) {
            return (DataTree<T>) nodes.get(nodeName.toString());
        } else if (this.nodes.containsKey(child)) {
            return (DataTree<T>) this.nodes.get(child).getNode(nodeName.cutFirst());
        } else {
            return null;
        }
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
    public boolean provides(String target, Name name) {
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
    public Object provide(String target, Name name) {
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

    public static class Builder<T> implements DataNode.Builder<T, DataTree<T>, Builder<T>> {

        private final DataTree<T> tree;

        /**
         * Create copy-builder for a DataNode. Does not change initial node
         *
         * @param node
         */
        public Builder(DataNode<T> node) {
            this.tree = cloneNode(node, node.getName());
        }

        private Builder(Class<T> type) {
            this.tree = new DataTree<>(type);
        }

        @Override
        public Class<T> type() {
            return tree.type();
        }

        @Override
        public Builder<T> setName(String name) {
            tree.name = name;
            return this;
        }

        @Override
        public Builder<T> setMeta(Meta meta) {
            if (meta == null) {
                tree.meta = Meta.empty();
            } else {
                tree.meta = meta;
            }
            return this;
        }

        @Override
        public Builder<T> putData(String key, Data<? extends T> data, boolean replace) {
            this.tree.putData(Name.of(key), data, replace);
            return this;
        }

        @Override
        public Builder<T> putNode(String key, DataNode<? extends T> node) {
            this.tree.putNode(Name.of(key), node);
            return this;
        }

        @Override
        public Builder<T> removeNode(String nodeName) {
            Name theName = Name.of(nodeName);
            DataTree parentTree;
            if (theName.length() == 1) {
                parentTree = tree;
            } else {
                parentTree = tree.getNode(theName.cutLast());
            }
            if (parentTree != null) {
                parentTree.nodes.remove(theName.getLast().toString());
            }
            return self();
        }

        @Override
        public Builder<T> removeData(String dataName) {
            Name theName = Name.of(dataName);
            DataTree parentTree;
            if (theName.length() == 1) {
                parentTree = tree;
            } else {
                parentTree = tree.getNode(theName.cutLast());
            }
            if (parentTree != null) {
                parentTree.data.remove(theName.getLast().toString());
            }
            return self();
        }

        @Override
        public DataTree<T> build() {
            return tree;
        }

        @Override
        public Builder<T> self() {
            return this;
        }

        @Override
        public Meta meta() {
            return this.tree.meta();
        }
    }

}
