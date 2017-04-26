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

import hep.dataforge.io.XMLMetaWriter;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The main building block of the DataForge.
 * <p>
 * TODO documentation here!
 * </p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class Meta implements Provider, Named, ValueProvider, Serializable, MetaProvider {

    private static final Meta EMPTY = new MetaBuilder("").build();

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
     *
     * @return
     */
    public static Meta empty() {
        return EMPTY;
    }

    /**
     * Return modifiable {@link MetaBuilder} witch copies data from this meta. Initial meta not changed.
     *
     * @return a {@link hep.dataforge.meta.MetaBuilder} object.
     */
    public MetaBuilder getBuilder() {
        return new MetaBuilder(this);
    }

    /**
     * Return the meta node with given name
     *
     * @param path
     * @return
     */
    public abstract List<? extends Meta> getMetaList(String path);

    @Provides(META_TARGET)
    public Optional<Meta> optMeta(String path) {
        return getMetaList(path).stream().findFirst().map(it -> it);
    }

//    /**
//     * Check if this meta has a node with given name
//     * @param name
//     * @return
//     */
//    public boolean hasMeta(String name) {
//        Collection<String> names = getNodeNames();
//        if (names.contains(name)) {
//            return true;
//        } else {
//            Name path = Name.of(name);
//            if (path.length() > 1) {
//                String head = path.getFirst().entry();
//                String tail = path.cutFirst().toString();
//                if (names.contains(head)) {
//                    return getMeta(head).hasMeta(tail);
//                } else {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        }
//    }

//    /**
//     * {@inheritDoc}
//     *
//     * @param name
//     * @return
//     */
//    @Override
//    public boolean hasValue(String name) {
//        if (getValueNames().contains(name)) {
//            return true;
//        } else {
//            Collection<String> names = getNodeNames();
//            Name path = Name.of(name);
//            if (path.length() > 1) {
//                String head = path.getFirst().entry();
//                String tail = path.cutFirst().toString();
//                if (names.contains(head)) {
//                    return getMeta(head).hasValue(tail);
//                } else {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        }
//    }

    public boolean isEmpty() {
        return this.getNodeNames().isEmpty() && this.getValueNames().isEmpty();
    }

    /**
     * List value names of direct descendants
     *
     * @return a {@link java.util.Collection} object.
     */
    @ProvidesNames(VALUE_TARGET)
    public abstract Collection<String> getValueNames();

    /**
     * List node names of direct descendants
     *
     * @return a {@link java.util.Collection} object.
     */
    @ProvidesNames(META_TARGET)
    public abstract Collection<String> getNodeNames();



    /**
     * Return a child node with given name or empty node if child node not found
     *
     * @param path
     * @return
     */
    public final Meta getMetaOrEmpty(String path) {
        return getMeta(path, Meta.empty());
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
            List<? extends Meta> elementItem = getMetaList(elementName);
            if (!other.hasMeta(elementName) || !elementItem.equals(other.getMetaList(elementName))) {
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
            hash = 59 * hash + Objects.hashCode(getMetaList(elementName));
        }
        return hash;
    }

    @Override
    public String toString() {
        return new XMLMetaWriter().writeString(this);
    }

}
