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
import hep.dataforge.exceptions.PathSyntaxException;
import hep.dataforge.names.Name;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An immutable representation of annotation node. Descendants could be mutable
 * <p>
 * TODO Documentation!
 * </p>
 *
 * @author Alexander Nozik
 */
public class MetaNode<T extends MetaNode> extends Meta {

    protected final Map<String, List<T>> nodes;
    protected String name;
    protected final Map<String, Value> values;

    /**
     * A static deep copy constructor for immutable annotations
     *
     * @param annotation
     * @return
     */
    public static MetaNode<MetaNode> from(Meta annotation) {
        MetaNode<MetaNode> res = new MetaNode<>(annotation.getName());

        Collection<String> valueNames = annotation.getValueNames();
        for (String valueName : valueNames) {
            res.values.put(valueName, annotation.getValue(valueName));
        }

        Collection<String> nodeNames = annotation.getNodeNames();
        for (String elementName : nodeNames) {
            List<MetaNode> item = annotation.getNodes(elementName).stream()
                    .<MetaNode>map((an) -> from(an))
                    .collect(Collectors.toList());
            res.nodes.put(elementName, new ArrayList<>(item));
        }
        return res;
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
            if (child.size() == 1 || !head.hasQuery()) {
                return child.get(0);
            } else {
                int num;
                try {
                    num = Integer.parseInt(head.getQuery());
                } catch (NumberFormatException ex) {
                    throw new PathSyntaxException("The query ([]) syntax for annotation must contain only integer numbers");
                }
                if (num < 0 || num >= child.size()) {
                    throw new NameNotFoundException(path.toString(), "No list element with given index");
                }
                return child.get(num);
            }
        } else {
            throw new NameNotFoundException(path.toString());
        }
    }
    
    /**
     * Return a node list using path notation and null if node does not exist
     * @param path
     * @return 
     */   
    protected List<T> getNodeItem(String path) {
        List<T> res;
        if (getNodeNames().contains(path)) {
            res = getChildNodeItem(path);
        } else {
            Name pathName = Name.of(path);
            if (pathName.length() > 1 && nodes.containsKey(pathName.getFirst().entry())) {
                res = getHead(pathName).getNodeItem(pathName.cutFirst().toString());
            } else {
                return null;
            }
        }
        return res;
    }

    /**
     * Return a value using path notation and null if it does not exist
     * @param path
     * @return 
     */
    protected Value getValueItem(String path) {
        Value res;
        if (getValueNames().contains(path)) {
            res = getChildValueItem(path);
        } else {
            Name pathName = Name.of(path);
            if (pathName.length() > 1 && nodes.containsKey(pathName.getFirst().entry())) {
                res = getHead(pathName).getValueItem(pathName.cutFirst().toString());
            } else {
                return null;
            }
        }
        return res;
    }

    /**
     * В случае передачи {@code "$all"} или {@code null} в качестве аргумента
     * возвращает всех прямых наследников
     *
     * @param name
     * @return
     */
    @Override
    public List<T> getNodes(String name) {
        List<T> item = getNodeItem(name);
        if (item == null) {
            throw new NameNotFoundException(name);
        } else {
            return Collections.unmodifiableList(item);
        }
    }

    @Override
    public T getNode(String name) {
        return getNodes(name).get(0);
    }

    @Override
    public Value getValue(String name) {
        Value item = getValueItem(name);
        if (item == null) {
            throw new NameNotFoundException(name);
        } else {
            return item;
        }
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
     * Return a direct descendant node with given name.
     * @param name
     * @return 
     */
    protected List<T> getChildNodeItem(String name) {
        if (getNodeNames().contains(name)) {
            return nodes.get(name);
        } else {
            throw new NameNotFoundException(name);
        }
    }

    /**
     * Return value of this node with given name
     * @param name
     * @return 
     */
    protected Value getChildValueItem(String name) {
        if (getValueNames().contains(name)) {
            return values.get(name);
        } else {
            throw new NameNotFoundException(name);
        }
    }

    protected boolean isValidElementName(String name) {
        return !(name.contains("[") || name.contains("]") || name.contains("$"));
    }

    public T getNode(String path, T def) {
        if (this.hasNode(path)) {
            return this.getNode(path);
        } else {
            return def;
        }
    }

}
