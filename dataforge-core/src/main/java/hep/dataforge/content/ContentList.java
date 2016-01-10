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
package hep.dataforge.content;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.WrongContentTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Простой контейнер для однотипных контентов в виде списка
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public class ContentList<T extends Named> extends NamedMetaHolder implements NamedGroup<T> {

    private final List<T> list;
    private Class<T> elementType;

    public ContentList(String name, Class<T> elementType, Iterable<T> sources) {
        super(name);
        this.list = new ArrayList<>();
        this.elementType = elementType;
        sources.forEach((s) -> add(s));
    }

    public ContentList(String name, Class<T> elementType, T... sources) {
        this(name, elementType, Arrays.asList(sources));
    }

    public ContentList(String name, Iterable<T> sources) {
        super(name);
        this.list = new ArrayList<>();
        this.elementType = null;
        sources.forEach((T s) -> {
            if (elementType == null) {
                elementType = (Class<T>) s.getClass();
            }
            add(s);
        });
    }

    public ContentList(String name, T... sources) {
        this(name, Arrays.asList(sources));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> asList() {
        return new ArrayList(list);
    }

    private void add(T content) throws WrongContentTypeException {
        if (elementType == null || elementType.isInstance(content)) {
            list.add(content);
        } else {
            throw new WrongContentTypeException(elementType, content.getClass());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(String name) throws NameNotFoundException {
        for (T child : list) {
            //Проверяем, что совпадает имя, или последний "этаж" имени
            if (child.getName().equalsIgnoreCase(name)
                    || child.getName().endsWith("." + name)) {
                return child;
            }
        }
        throw new NameNotFoundException(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class type() {
        if (elementType != null) {
            return elementType;
        } else if (list.size() > 0) {
            return list.get(0).getClass();
        } else {
            throw new IllegalStateException("Can't infere content type for the content list");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(String name) {
        try {
            get(name);
            return true;
        } catch (NameNotFoundException ex) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

}
