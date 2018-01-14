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
package hep.dataforge.names;

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NonEmptyMetaMorphException;
import hep.dataforge.meta.Meta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * The fast random access implementation of Names based on ArrayList.
 *
 * @author Alexander Nozik
 */
public class NameList implements Names {

    private final ArrayList<String> nameList = new ArrayList<>();

    /**
     * An index cache to make calls of {@code getNumberByName} faster
     */
    private transient Map<String, Integer> indexCache = new HashMap<>();

    public NameList(String... list) {
        try {
            addNames(java.util.Arrays.asList(list));
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public NameList(Names names) {
        try {
            addNames(names);
        } catch (NamingException ex) {
            throw new Error(ex);
        }
    }

    public NameList(Iterable<String> list) throws NamingException {
        addNames(list);
    }

    public NameList(Meta meta){
        if (!this.nameList.isEmpty()) {
            throw new NonEmptyMetaMorphException(getClass());
        } else {
            this.nameList.addAll(Arrays.asList(meta.getStringArray("names")));
        }
    }

    /**
     * Check for duplicates
     *
     * @param names a {@link java.lang.Iterable} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    public final void addNames(Iterable<String> names) throws NamingException {
        for (String name : names) {
            addName(name);
        }
    }

    public final void addName(String name) {
        if (!nameList.contains(name)) {
            nameList.add(name);
        } else {
            throw new NamingException("Duplicate names in a Names");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(String... names) {
        List<String> list = asList();
        boolean res = true;
        for (String name : names) {
            res = res && list.contains(name);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Names names) {
        List<String> list = asList();
        boolean res = true;
        for (String name : names) {
            res = res && list.contains(name);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return nameList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get(int i) {
        return this.nameList.get(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberByName(String str) {
        return indexCache.computeIfAbsent(str, (name) -> nameList.indexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Iterator<String> iterator() {
        return this.nameList.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] asArray() {
        return nameList.toArray(new String[size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> asList() {
        return Collections.unmodifiableList(this.nameList);
    }

    @Override
    public Stream<String> stream() {
        return this.nameList.stream();
    }

}
