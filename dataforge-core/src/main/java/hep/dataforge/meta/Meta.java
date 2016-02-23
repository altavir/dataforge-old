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

import hep.dataforge.content.Named;
import hep.dataforge.exceptions.TargetNotProvidedException;
import hep.dataforge.names.Name;
import hep.dataforge.navigation.AbstractProvider;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The main building block of the DataForge.
 * <p>
 * TODO documentation here!
 * </p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class Meta extends AbstractProvider implements Named, ValueProvider, Serializable {

    /**
     * Build an empty annotation with given name FIXME make a separate simple
     * class for empty annotation for performance
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    public static Meta buildEmpty(String name) {
        return new MetaBuilder(name).build();
    }
    
    /**
     * Empty anonymous meta
     * @return 
     */
    public static Meta buildEmpty() {
        return new MetaBuilder("").build();
    }    

    /**
     * Возвращает билдер, который работает с копией этой аннотации
     *
     * @return a {@link hep.dataforge.meta.MetaBuilder} object.
     */
    public MetaBuilder getBuilder() {
        return new MetaBuilder(this);
    }

    public abstract Meta getNode(String path);

    /**
     * В случае передачи {@code "$all"} или {@code null} в качестве аргумента
     * возвращает всех прямых наследников
     *
     * @param name
     * @return
     */
    public abstract List<? extends Meta> getNodes(String name);

    public String[] getStringArray(String name) {
        List<Value> vals = getValue(name).listValue();
        String[] res = new String[vals.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = vals.get(i).stringValue();
        }
        return res;
    }

    /**
     * {@inheritDoc}
     *
     * @param path
     * @return
     */
    @Override
    public abstract Value getValue(String path);

    public boolean hasChildren() {
        return !getNodeNames().isEmpty();
    }

    public boolean hasNode(String name) {
        Collection<String> names = getNodeNames();
        if (names.contains(name)) {
            return true;
        } else {
            Name path = Name.of(name);
            if (path.length() > 1) {
                String head = path.getFirst().entry();
                String tail = path.cutFirst().toString();
                if (names.contains(head)) {
                    return getNode(head).hasNode(tail);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param name
     * @return
     */
    @Override
    public boolean hasValue(String name) {
        if (getValueNames().contains(name)) {
            return true;
        } else {
            Collection<String> names = getNodeNames();
            Name path = Name.of(name);
            if (path.length() > 1) {
                String head = path.getFirst().entry();
                String tail = path.cutFirst().toString();
                if (names.contains(head)) {
                    return getNode(head).hasValue(tail);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public boolean isEmpty() {
        return this.getNodeNames().isEmpty() && this.getValueNames().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object provide(String target, Name name) {
        switch (target) {
            case "value":
                return getValue(name.toString());
            case "annotation":
                return getNode(name.toString());
            default:
                throw new TargetNotProvidedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean provides(String target, Name name) {
        switch (target) {
            case "value":
                return hasValue(name.toString());
            case "annotation":
                return hasNode(name.toString());
            default:
                return false;
        }

    }

    /**
     * List value names of direct descendants
     *
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<String> getValueNames();

    /**
     * List node names of direct descendants
     *
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<String> getNodeNames();

    public Meta getNode(String path, Meta def) {
        if (this.hasNode(path)) {
            return getNode(path);
        } else {
            return def;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        } else if (obj instanceof Meta) {
            Meta other = (Meta) obj;
            if (!Objects.equals(getName(), other.getName())) {
                return false;
            }
            return equalsIgnoreName(other);
        } else {
            return false;
        }
    }

    /**
     * Check if two annotations are equal ignoring their names. Names of child
     * elements are not ignored
     *
     * @param other
     * @return
     */
    public boolean equalsIgnoreName(Meta other) {
        for (String valueName : getValueNames()) {
            Value value = getValue(valueName);
            if (!other.hasValue(valueName) || !value.equals(other.getValue(valueName))) {
                return false;
            }
        }
        for (String elementName : getNodeNames()) {
            List<? extends Meta> elementItem = getNodes(elementName);
            if (!other.hasNode(elementName) || !elementItem.equals(other.getNodes(elementName))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(getName());
        for (String valueName : getValueNames()) {
            hash = 59 * hash + Objects.hashCode(getValue(valueName));
        }
        for (String elementName : getNodeNames()) {
            hash = 59 * hash + Objects.hashCode(getNodes(elementName));
        }
        return hash;
    }
}
