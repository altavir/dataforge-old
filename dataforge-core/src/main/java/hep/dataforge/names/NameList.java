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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The fast random access implementation of Names based on ArrayList.
 *
 * @author Alexander Nozik
 */
public class NameList implements Names, Serializable {

    protected ArrayList<String> nameList = new ArrayList<>();

    /**
     * <p>
     * Constructor for NameList.</p>
     *
     * @param list a {@link java.lang.String} object.
     */
    public NameList(String... list) {
        try {
            addNames(java.util.Arrays.asList(list));
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * <p>
     * Constructor for NameList.</p>
     *
     * @param named a {@link hep.dataforge.names.Names} object.
     */
    public NameList(Names named) {
        try {
            addNames(named);
        } catch (NamingException ex) {
            throw new Error(ex);
        }
    }

    /**
     * <p>
     * Constructor for NameList.</p>
     *
     * @param list a {@link java.lang.Iterable} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    public NameList(Iterable<String> list) throws NamingException {
        addNames(list);
    }

    /**
     * Проверка на дублирующиеся имена
     *
     * @param names a {@link java.lang.Iterable} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    public final void addNames(Iterable<String> names) throws NamingException {
        for (String name : names) {
            addName(name);
        }
    }

    /**
     * <p>
     * addName.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public final void addName(String name) {
        if (!nameList.contains(name)) {
            nameList.add(name);
        } else {
            throw new NamingException("Dublicate names in a Named");
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
    public int getDimension() {
        return nameList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(int i) {
        return this.nameList.get(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberByName(String str) {
        return nameList.indexOf(str);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        return this.nameList.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] asArray() {
        return nameList.toArray(new String[getDimension()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> asList() {
        return (List<String>) nameList.clone();
    }
}
