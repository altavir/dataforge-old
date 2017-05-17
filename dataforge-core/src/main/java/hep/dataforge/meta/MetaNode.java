/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.meta;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NonEmptyMetaMorphException;
import hep.dataforge.names.Name;
import hep.dataforge.providers.Provides;
import hep.dataforge.utils.MetaMorph;
import hep.dataforge.values.Value;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An immutable representation of annotation node. Descendants could be mutable
 * <p>
 * TODO Documentation!
 * </p>
 *
 * @author Alexander Nozik
 */
public class MetaNode<T extends MetaNode> extends Meta implements MetaMorph {
    private static final long serialVersionUID = 1L;

    protected final Map<String, List<T>> nodes;
    protected String name;
    protected final Map<String, Value> values;

    /**
     * A static deep copy constructor for immutable annotations
     *
     * @param meta
     * @return
     */
    public static MetaNode<MetaNode> from(Meta meta) {
        MetaNode<MetaNode> res = new MetaNode<>(meta.getName());

        Collection<String> valueNames = meta.getValueNames();
        valueNames.stream().forEach((valueName) -> {
            res.values.put(valueName, meta.getValue(valueName));
        });

        Collection<String> nodeNames = meta.getNodeNames();
        nodeNames.stream().forEach((elementName) -> {
            List<MetaNode> item = meta.getMetaList(elementName).stream()
                    .<MetaNode>map((an) -> from(an))
                    .collect(Collectors.toList());
            res.nodes.put(elementName, new ArrayList<>(item));
        });
        return res;
    }

    public MetaNode() {
        this("");
    }

    public MetaNode(String name) {
        this.name = name;
        values = new LinkedHashMap<>();
        nodes = new LinkedHashMap<>();
    }

    /**
     * get the node corresponding to the first token of given path
     *
     * @param path
     * @return
     */
    protected T getHead(Name path) {
        Collection<String> names = getNodeNames();
        Name head = path.getFirst();
        if (names.contains(head.entry())) {
            List<T> child = getChildNodeItem(head.entry());
            if (head.hasQuery()) {
                child = MetaUtils.applyQuery(child, head.getQuery());
            }
            return child.get(0);
        } else {
            throw new NameNotFoundException(path.toString());
        }
    }

    @Provides(META_TARGET)
    @Override
    public Optional<T> optMeta(String path) {
        return getMetaList(path).stream().findFirst().map(it -> it);
    }

    /**
     * Return a node list using path notation and null if node does not exist
     *
     * @param path
     * @return
     */
    protected List<T> getNodeItem(Name path) {
        List<T> res;
        if (path.length() == 1) {
            String pathStr = path.ignoreQuery().toString();
            if (getNodeNames().contains(pathStr)) {
                res = getChildNodeItem(pathStr);
            } else {
                res = null;
            }
        } else if (path.length() > 1 && nodes.containsKey(path.getFirst().entry())) {
            res = getHead(path).getNodeItem(path.cutFirst());
        } else {
            res = null;
        }

        /**
         * Filtering nodes using query
         */
        if (res != null) {
            if (path.hasQuery()) {
                res = MetaUtils.applyQuery(res, path.getQuery());
            }
            if (res.isEmpty()) {
                res = null;
            }
        }

        return res;
    }

    /**
     * Return a value using path notation and null if it does not exist
     *
     * @param path
     * @return
     */
    protected Value getValueItem(Name path) {
        Value res;
        if (path.length() == 1) {
            String pathStr = path.toString();
            if (getValueNames().contains(pathStr)) {
                res = getChildValue(pathStr);
            } else {
                return null;
            }
        } else if (path.length() > 1 && nodes.containsKey(path.getFirst().entry())) {
            res = getHead(path).getValueItem(path.cutFirst());
        } else {
            return null;
        }
        return res;

    }

    /**
     * Return a list of all nodes for given name filtered by query if it exists.
     * If node not found or there are no results for the query, the exception is
     * thrown.
     *
     * @param name
     * @return
     */
    @Override
    public List<T> getMetaList(String name) {
        Name n = Name.of(name);
        List<T> item = getNodeItem(n);
        if (item == null) {
            return Collections.emptyList();
        } else {
            return item;
        }
    }

    @Override
    public T getMeta(String path) {
        return getMetaList(path).stream().findFirst().orElseThrow(() -> new NameNotFoundException(path));
    }

    @Override
    public Optional<Value> optValue(String name) {
        return Optional.ofNullable(getValueItem(Name.of(name)));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Collection<String> getNodeNames() {
        return this.nodes.keySet();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Collection<String> getValueNames() {
        return this.values.keySet();
    }

    /**
     * Return a direct descendant node with given name. Return null if it is not found.
     *
     * @param name
     * @return
     */
    protected List<T> getChildNodeItem(String name) {
        return nodes.get(name);
    }

    /**
     * Return value of this node with given name. Return null if it is not found.
     *
     * @param name
     * @return
     */
    protected Value getChildValue(String name) {
        return values.get(name);
    }

    protected boolean isValidElementName(String name) {
        return !(name.contains("[") || name.contains("]") || name.contains("$"));
    }

    @Override
    public Meta toMeta() {
        return this;
    }

    @Override
    public void fromMeta(Meta meta) {
        if (!isEmpty()) {
            throw new NonEmptyMetaMorphException(getClass());
        }
        this.name = meta.getName();

        MetaNode node = (meta instanceof MetaNode) ? (MetaNode) meta : from(meta);

        this.values.putAll(node.values);
        this.nodes.putAll(node.nodes);
    }

//    /**
//     * A stream containing pairs
//     *
//     * @param prefix
//     * @return
//     */
//    private Stream<Pair<String, T>> nodeStream(String prefix) {
//        return Stream.concat(Stream.of(new Pair<>(prefix, this)),
//                this.getNodeNames().stream().flatMap(nodeName -> {
//                    List<? extends T> metaList = this.getMetaList(nodeName);
//                    String nodePrefix;
//                    if (prefix == null || prefix.isEmpty()) {
//                        nodePrefix = nodeName;
//                    } else {
//                        nodePrefix = prefix + "." + nodeName;
//                    }
//                    return IntStream.range(0, metaList.size()).boxed()
//                            .flatMap(i -> {
//                                String subPrefix = String.format("%s[%d]", nodePrefix, i);
//                                T subNode = metaList.get(i);
//                                return nodeStream(subPrefix, subNode);
//                            });
//                })
//        );
//    }
//
//    public Stream<Pair<String, T>> nodeStream() {
//        return nodeStream("");
//    }
//
//    public Stream<Pair<String, Value>> valueStream() {
//        return nodeStream().flatMap((Pair<String, T> entry) -> {
//            String key = entry.getKey();
//            Meta childMeta = entry.getValue();
//            return childMeta.getValueNames().stream()
//                    .map((String valueName) -> new Pair<>(key + "." + valueName, childMeta.getValue(valueName)));
//        });
//    }

}
