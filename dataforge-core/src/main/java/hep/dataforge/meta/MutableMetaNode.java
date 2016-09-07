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

import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.names.Name;
import hep.dataforge.utils.GenericBuilder;
import hep.dataforge.values.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A mutable annotation node equipped with observers.
 *
 * @author Alexander Nozik
 */
@SuppressWarnings("unchecked")
public abstract class MutableMetaNode<T extends MutableMetaNode> extends MetaNode<T>
        implements Serializable, GenericBuilder<Meta, T> {

    protected T parent;

    public MutableMetaNode(String name) {
        super(name);
        this.parent = null;
    }

    public Name getFullName() {
        if (parent == null) {
            return Name.of(getName());
        } else {
            return Name.join(parent.getFullName(), Name.of(getName()));
        }
    }

    /**
     * Notify all observers that element is changed
     *
     * @param name
     * @param oldItem
     * @param newItem
     */
    protected void notifyNodeChanged(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem) {
        if (parent != null) {
            parent.notifyNodeChanged(getName() + "." + name, oldItem, newItem);
        }
    }

    /**
     * Notify all observers that value is changed
     *
     * @param name
     * @param oldItem
     * @param newItem
     */
    protected void notifyValueChanged(String name, Value oldItem, Value newItem) {
        if (parent != null) {
            parent.notifyValueChanged(getName() + "." + name, oldItem, newItem);
        }
    }

    /**
     * Return type checked state of this node
     *
     * @return
     */
    @Override
    public abstract T self();

    protected T getParent() {
        return parent;
    }

    /**
     * Add a copy of given meta to the node list with given name. Create a new
     * one if it does not exist
     *
     * @param element
     * @param notify  notify listeners
     */
    public T putNode(String name, Meta element, boolean notify) {
        if (!isValidElementName(name)) {
            throw new NamingException(String.format("\"%s\" is not a valid element name in the annotation", name));
        }

        T newNode = transformNode(name, element);
        List<T> list = this.getChildNodeItem(name);
        List<T> oldList = list != null ? new ArrayList<>(list) : null;
        if (list == null) {
            List<Meta> newList = new ArrayList<>();
            newList.add(newNode);
            this.setNodeItem(name, newList);
            list = this.getChildNodeItem(name);
        } else {
            list.add(newNode);
        }
        if (notify) {
            notifyNodeChanged(element.getName(), oldList, list);
        }
        return self();
    }

    /**
     * putNode(element,true)
     *
     * @param element
     * @return
     */
    public T putNode(Meta element) {
        if (element.isAnonimous()) {
            throw new AnonymousNotAlowedException();
        }
        return putNode(element.getName(), element, true);
    }

    /**
     * Same as {@code putNode(Meta)}, but also renames new node
     *
     * @param name
     * @param element
     * @return
     */
    public T putNode(String name, Meta element) {
        return putNode(name, element, true);
    }

    /**
     * Add new value to the value item with the given name. Create new one if it
     * does not exist. null arguments are ignored (Value.NULL is still could be
     * used)
     *
     * @param name
     * @param value
     * @param notify notify listeners
     */
    public T putValue(String name, Value value, boolean notify) {
        if (!isValidElementName(name)) {
            throw new NamingException(String.format("'%s' is not a valid element name in the meta", name));
        }

        if (value != null) {
            if (hasValue(name)) {
                Value oldValue = getValue(name);

                List<Value> list = new ArrayList(oldValue.listValue());
                list.add(value);

                Value newValue = Value.of(list);

                setValueItem(name, newValue);

                if (notify) {
                    notifyValueChanged(name, oldValue, newValue);
                }
            } else {
                setValueItem(name, value);
            }
        }
        return self();
    }

    /**
     * putValue(name, value, true)
     *
     * @param name
     * @param value
     * @return
     */
    public T putValue(String name, Value value) {
        return putValue(name, value, true);
    }

    /**
     * <p>
     * setNode.</p>
     *
     * @param element
     */
    public T setNode(Meta element) {
        if (element.isAnonimous()) {
            throw new AnonymousNotAlowedException();
        }

        String nodeName = element.getName();
        if (!isValidElementName(nodeName)) {
            throw new NamingException(String.format("\"%s\" is not a valid element name in the meta", nodeName));
        }
        this.setNode(nodeName, element);
        return self();
    }

    /**
     * Set or replace current node or node list with this name
     *
     * @param name
     * @param elements
     * @param notify
     * @return
     */
    public T setNode(String name, List<? extends Meta> elements, boolean notify) {
        if (elements != null && !elements.isEmpty()) {
            List<T> oldNodeItem;
            if (hasNode(name)) {
                oldNodeItem = new ArrayList<>(getNodes(name));
            } else {
                oldNodeItem = null;
            }
            setNodeItem(name, elements);
            if (notify) {
                notifyNodeChanged(name, oldNodeItem, getNodes(name));
            }
        } else {
            removeNode(name);
        }
        return self();
    }

    /**
     * setNode(name,elements,true)
     *
     * @param name
     * @param elements
     * @return
     */
    public T setNode(String name, List<? extends Meta> elements) {
        return setNode(name, elements, true);
    }

    /**
     * Добавляет новый элемент, стирая старый
     *
     * @param name
     * @param elements
     */
    public T setNode(String name, Meta... elements) {
        List<Meta> res = new ArrayList<>();
        res.addAll(Arrays.asList(elements));
        return setNode(name, res);
    }

    /**
     * Replace a value item with given name.
     *
     * @param name
     * @param value
     */
    public T setValue(String name, Value value, boolean notify) {
        if (value == null || value.isNull()) {
            removeValue(name);
        } else {
            Value oldValueItem;
            if (hasValue(name)) {
                oldValueItem = getValue(name);
            } else {
                oldValueItem = null;
            }
            setValueItem(name, value);
            if (notify) {
                notifyValueChanged(name, oldValueItem, value);
            }
        }
        return self();
    }

    /**
     * setValue(name, value, true)
     *
     * @param name
     * @param value
     * @return
     */
    public T setValue(String name, Value value) {
        return setValue(name, value, true);
    }

    public T setValue(String name, Object object) {
        return setValue(name, Value.of(object));
    }

    /**
     * Adds new value to the list of values with given name. Ignores null value.
     * Does not replace old Value!
     *
     * @param name
     * @param value
     * @return
     */
    public T putValue(String name, Object value) {
        putValue(name, Value.of(value));
        return self();
    }

    public T putValues(String name, Object[] values) {
        if (values != null) {
            for (Object obj : values) {
                putValue(name, obj);
            }
        }
        return self();
    }

    /**
     * Rename this node
     *
     * @param name
     */
    protected T setName(String name) {
        if (parent != null) {
            throw new RuntimeException("Can't rename attached node");
        }
        this.name = name;
        return self();
    }

    /**
     * Remove node list at given path (including descending tree) and notify
     * listener
     *
     * @param path
     */
    public T removeNode(String path) {
        if (hasNode(path)) {
            List<T> oldNode = getNodes(path);

            if (nodes.containsKey(path)) {
                nodes.remove(path);
            } else {
                Name namePath = Name.of(path);
                if (namePath.length() > 1) {
                    //FIXME many path to string and string to path conversions
                    getHead(namePath).removeNode(namePath.cutFirst().toString());
                }
            }

            notifyNodeChanged(path, oldNode, null);
        }
        return self();
    }

    /**
     * Replace or remove given direct descendant child node if it is present and
     * notify listeners.
     *
     * @param child
     */
    public void replaceChildNode(T child, Meta node) {
        String nodeName = child.getName();
        if (hasNode(nodeName)) {
            List<T> oldNode = getNodes(nodeName);
            int index = nodes.get(nodeName).indexOf(child);
            if (node == null) {
                nodes.get(nodeName).remove(index);
            } else {
                nodes.get(nodeName).set(index, transformNode(child.getName(), node));
            }
            notifyNodeChanged(nodeName, oldNode, getNodes(nodeName));
        }
    }

    /**
     * Remove value with given path and notify listener
     *
     * @param path
     */
    public void removeValue(String path) {
        if (this.hasValue(path)) {
            Value oldValue = getValue(path);
            if (values.containsKey(path)) {
                values.remove(path);
            } else {
                Name namePath = Name.of(path);
                if (namePath.length() > 1) {
                    getHead(namePath).removeValue(namePath.cutFirst().toString());
                }
            }
            notifyValueChanged(path, oldValue, null);
        }
    }

    /**
     * Replaces node with given path with given item or creates new one
     *
     * @param path
     * @param elements
     */
    protected void setNodeItem(String path, List<? extends Meta> elements) {
        if (!nodes.containsKey(path)) {
            Name namePath = Name.of(path);
            if (namePath.length() > 1) {
                String headName = namePath.getFirst().entry();
                T headNode;
                if (nodes.containsKey(headName)) {
                    headNode = getHead(namePath);
                } else {
                    headNode = createChildNode(headName);
                    attachNode(headNode);
                }

                headNode.setNodeItem(namePath.cutFirst().toString(), elements);
            } else {
                //single token path
                this.nodes.put(path, transformNodeItem(path, elements));
            }
        } else {
            // else reset contents of the node
            this.nodes.put(path, transformNodeItem(path, elements));
        }
    }

    protected void setValueItem(String path, Value value) {
        if (!this.values.containsKey(path)) {
            Name namePath = Name.of(path);
            if (namePath.length() > 1) {
                String headName = namePath.getFirst().entry();
                T headNode;
                if (nodes.containsKey(headName)) {
                    headNode = getHead(namePath);
                } else {
                    headNode = createChildNode(headName);
                    attachNode(headNode);
                }
                headNode.setValueItem(namePath.cutFirst().toString(), value);
            } else {
                //single token path
                this.values.put(path, value);
            }
        } else {
            // else reset contents of the node
            this.values.put(path, value);
        }
    }

    /**
     * Transform list of nodes changing their name and parent
     *
     * @param name
     * @param item
     * @return
     */
    private List<T> transformNodeItem(String name, List<? extends Meta> item) {
        List<T> res = new ArrayList<>();
        item.stream().map((an) -> transformNode(name, an)).map((el) -> {
            el.parent = this;
            return el;
        }).forEach((el) -> {
            res.add(el);
        });
        return res;
    }

    private T transformNode(String name, Meta node) {
        T el = cloneNode(node);
        el.setName(name);
        el.parent = this;
        return el;
    }

    /**
     * Create but do not attach new child node
     *
     * @param name
     * @return
     */
    protected abstract T createChildNode(String name);

    /**
     * Create a deep copy of the node but do not set parent or name. Deep copy
     * does not clone listeners
     *
     * @param node
     * @return
     */
    protected abstract T cloneNode(Meta node);

    /**
     * Attach node item without transformation. Each node's parent is changed to
     * this
     *
     * @param name
     * @param nodes
     */
    public void attachNodeItem(String name, List<T> nodes) {
        nodes.stream().forEach((T node) -> {
            node.parent = this;
            node.name = name;
        });
        List<T> oldList = this.nodes.get(name);
        this.nodes.put(name, nodes);
        notifyNodeChanged(name, oldList, nodes);
    }

    /**
     * Add new node to the current list of nodes with the given name. Replace
     * its parent with this.
     *
     * @param node
     */
    public void attachNode(T node) {
        if (node == null) {
            throw new IllegalArgumentException("Can't attach null node");
        }
        String nodeName = node.getName();
        node.parent = this;
        List<T> list;
        if (nodes.containsKey(nodeName)) {
            list = nodes.get(nodeName);
        } else {
            list = new ArrayList<>();
            nodes.put(nodeName, list);
        }
        List<T> oldList = new ArrayList<>(list);
        list.add(node);
        notifyNodeChanged(nodeName, oldList, list);
    }

    @Override
    public Meta build() {
        return this;
    }

}