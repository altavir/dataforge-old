/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import org.slf4j.LoggerFactory;

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
public class DataTree<T> implements DataNode<T> {

    private final Class<T> type;
    private final Map<String, DataTree<T>> nodes = new HashMap<>();
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
        this.meta = tree.meta;
        this.name = as;
        tree.nodes.forEach((String key, DataTree<T> tree1) -> {
            nodes.put(key, new DataTree<>(tree1, key));
        });
        this.data.putAll(tree.data);
    }

    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    /**
     * A general non-restricting tree builder
     *
     * @return
     */
    public static Builder<Object> builder() {
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
        if (name.getLength() == 1) {
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
    @SuppressWarnings("unchecked")
    public Optional<Data<T>> optData(String dataName) {
        return dataStream(true)
                .filter(it -> it.getName().equals(dataName))
                .findFirst()
                .map(it -> (Data<T>) it);
    }

    /**
     * Private method to add data to the node
     *
     * @param name
     * @param data
     */
    protected void putData(Name name, Data<? extends T> data, boolean replace) {
        if (name.getLength() == 0) {
            throw new IllegalArgumentException("Name must not be empty");
        } else if (name.getLength() == 1) {
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
        if (name.getLength() == 0) {
            throw new IllegalArgumentException("Can't put node with empty name");
        } else if (name.getLength() == 1) {
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
        if (type().isAssignableFrom(node.type())) {
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
        return nodes.entrySet().stream().flatMap((Map.Entry<String, DataTree<T>> nodeEntry) -> {
            Stream<DataNode<T>> nodeItself = Stream.of(
                    new NodeWrapper<>(nodeEntry.getValue(), parentName.toString(), parentMeta.cleanup())
            );

            Name childName = parentName.append(nodeEntry.getKey());
            Laminate childMeta = parentMeta.withFirstLayer(nodeEntry.getValue().meta());
            Stream<DataNode<? extends T>> childStream = nodeEntry.getValue().nodeStream(childName, childMeta);

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
                                .dataStream(subNodeName, nodeMeta.withFirstLayer(nodeEntry.getValue().meta()), true);
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
    public Optional<DataNode<T>> optNode(String nodeName) {
        return Optional.ofNullable(getNode(Name.of(nodeName)));
    }

    @SuppressWarnings("unchecked")
    DataTree<T> getNode(Name nodeName) {
        String child = nodeName.getFirst().toString();
        if (nodeName.getLength() == 1) {
            return nodes.get(nodeName.toString());
        } else if (this.nodes.containsKey(child)) {
            return this.nodes.get(child).getNode(nodeName.cutFirst());
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

    public static class Builder<T> implements DataNode.Builder<T, DataTree<T>, Builder<T>> {

        private DataTree<T> tree;

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
            if (key == null || key.isEmpty()) {
                if (!tree.isEmpty()) {
                    LoggerFactory.getLogger(getClass()).warn("Overriding non-empty data tree root");
                }
                tree = cloneNode(node.checked(tree.type), node.getName());
            } else {
                this.tree.putNode(Name.of(key), node);
            }
            return this;
        }

        @Override
        public Builder<T> removeNode(String nodeName) {
            Name theName = Name.of(nodeName);
            DataTree parentTree;
            if (theName.getLength() == 1) {
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
            if (theName.getLength() == 1) {
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

        public boolean isEmpty() {
            return this.tree.isEmpty();
        }
    }

}
