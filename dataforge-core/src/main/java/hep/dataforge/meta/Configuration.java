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

import hep.dataforge.utils.ReferenceRegistry;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A mutable annotation that exposes MuttableAnnotationNode edit methods and
 * adds automatically inherited observers.
 *
 * @author Alexander Nozik
 */
public class Configuration extends MutableMetaNode<Configuration> {

    /**
     * The meta node or value descriptor tag to mark an element non
     * configurable. It could be set only once.
     */
    public static final String FINAL_TAG = "final";

    protected final ReferenceRegistry<ConfigChangeListener> observers = new ReferenceRegistry<>();

    /**
     * Create empty root configuration
     *
     * @param name
     */
    public Configuration(String name) {
        super(name);
    }

    public Configuration() {
        super();
    }

    /**
     * Create a root configuration populated by given meta
     *
     * @param meta
     */
    public Configuration(Meta meta) {
        super(meta.getName());
        meta.getValueNames(true).forEach(valueName -> {
            setValueItem(valueName, meta.getValue(valueName));
        });

        meta.getNodeNames(true).forEach(nodeName -> {
            List<Configuration> item = meta.getMetaList(nodeName).stream()
                    .map(Configuration::new)
                    .collect(Collectors.toList());
            setNodeItem(nodeName, new ArrayList<>(item));

        });
    }

    /**
     * Notify all observers that element is changed
     *
     * @param name
     * @param oldItem
     * @param newItem
     */
    @Override
    protected void notifyNodeChanged(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem) {
        observers.forEach((ConfigChangeListener obs) -> obs.notifyNodeChanged(name, oldItem, newItem));
        super.notifyNodeChanged(name, oldItem, newItem);
    }

    /**
     * Notify all observers that value is changed
     *
     * @param name
     * @param oldItem
     * @param newItem
     */
    @Override
    protected void notifyValueChanged(String name, Value oldItem, Value newItem) {
        observers.forEach((ConfigChangeListener obs)
                -> obs.notifyValueChanged(name, oldItem, newItem));
        super.notifyValueChanged(name, oldItem, newItem);
    }

    /**
     * Add new observer for this configuration
     *
     * @param observer
     * @param strongReference if true, then configuration prevents observer from
     *                        being recycled by GC
     */
    public void addObserver(ConfigChangeListener observer, boolean strongReference) {
        this.observers.add(observer, strongReference);
    }

    /**
     * addObserver(observer, true)
     *
     * @param observer
     */
    public void addObserver(ConfigChangeListener observer) {
        addObserver(observer, true);
    }

    //PENDING add value observers inheriting value class by wrapper

    /**
     * Remove an observer from this configuration
     *
     * @param observer
     */
    public void removeObserver(ConfigChangeListener observer) {
        this.observers.remove(observer);
    }

    /**
     * update this configuration replacing all old values and nodes
     *
     * @param annotation
     */
    public void update(Meta annotation) {
        if (annotation != null) {
            annotation.getValueNames().forEach((valueName) -> {
                setValue(valueName, annotation.getValue(valueName));
            });

            annotation.getNodeNames().forEach((elementName) -> {
                setNode(elementName,
                        annotation
                                .getMetaList(elementName)
                                .stream()
                                .map((el) -> new Configuration(el))
                                .collect(Collectors.toList())
                );
            });
        }
    }

    @Override
    public Configuration self() {
        return this;
    }

    @Override
    public Configuration putNode(Meta an) {
        super.putNode(new Configuration(an));
        return self();
    }

    /**
     * Return existing node if it exists, otherwise builder and attach empty child
     * node
     *
     * @param name
     * @return
     */
    public Configuration requestNode(String name) {
        return optMeta(name).map(it -> (Configuration) it).orElseGet(() -> {
            Configuration child = createChildNode(name);
            super.attachNode(child);
            return child;
        });
    }

    @Override
    protected Configuration createChildNode(String name) {
        return new Configuration(name);
    }

    @Override
    protected Configuration cloneNode(Meta node) {
        return new Configuration(node);
    }

    @Override
    @Nullable
    public Configuration getParent() {
        return super.getParent();
    }

    public Configuration rename(String newName) {
        return super.setName(newName);
    }
}
