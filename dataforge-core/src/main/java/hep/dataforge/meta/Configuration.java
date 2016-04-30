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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A mutable annotation that exposes MuttableAnnotationNode edit methods and
 * adds automatically inherited observers.
 *
 * @author Alexander Nozik
 */
public class Configuration extends MuttableMetaNode<Configuration> {

    protected final ReferenceRegistry<ConfigChangeListener> observers = new ReferenceRegistry<>();

    /**
     * Create empty root configuration
     *
     * @param name
     */
    public Configuration(String name) {
        super(name);
    }

    /**
     * Create a root configuration populated by given meta
     *
     * @param meta
     */
    public Configuration(Meta meta) {
        super(meta.getName());
        Collection<String> valueNames = meta.getValueNames();
        for (String valueName : valueNames) {
            setValueItem(valueName, meta.getValue(valueName));
        }

        Collection<String> elementNames = meta.getNodeNames();
        for (String elementName : elementNames) {
            List<Configuration> item = meta.getNodes(elementName).stream()
                    .<Configuration>map((an) -> new Configuration(an))
                    .collect(Collectors.toList());
            setNodeItem(elementName, new ArrayList<>(item));
        }
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
        observers.stream().forEach((ConfigChangeListener obs) -> obs.notifyElementChanged(name, oldItem, newItem));
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
        observers.stream().forEach((ConfigChangeListener obs)
                -> obs.notifyValueChanged(name, oldItem, newItem));
        super.notifyValueChanged(name, oldItem, newItem);
    }

    /**
     * Add new observer for this configuration
     *
     * @param observer
     * @param strongReference if true, then configuration prevents observer from
     * being recycled by GC
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
        annotation.getValueNames().stream().forEach((valueName) -> {
            setValue(valueName, annotation.getValue(valueName));
        });

        annotation.getNodeNames().stream().forEach((elementName) -> {
            setNode(elementName,
                    annotation
                    .getNodes(elementName)
                    .stream()
                    .<Configuration>map((el) -> new Configuration(el))
                    .collect(Collectors.toList())
            );
        });
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
     * Return existing node if it exists, otherwise build and attach empty child
     * node
     *
     * @param name
     * @return
     */
    public Configuration requestNode(String name) {
        if (hasNode(name)) {
            return getNode(name);
        } else {
            Configuration child = createChildNode(name);
            super.attachNode(child);
            return child;
        }
    }

    @Override
    protected Configuration createChildNode(String name) {
        return new Configuration(name);
    }

    @Override
    protected Configuration cloneNode(Meta node) {
        return new Configuration(node);
    }

}
