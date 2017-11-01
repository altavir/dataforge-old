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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Класс для работы с именами. В именах запрещены символы "/" и "::", так как
 * они могут мешать распознаванию сегментов пути. Имя подчиняется стандартной
 * нотации Java : {@code namespace:token1.token2.token3}, где namespace - не
 * обязательный иденитификатор пространства имен.
 * </p>
 * <p>
 * Фрагмент имени (token) не может содержать скобки (любого типа) и знаки
 * препинания (",.!?")
 * </p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Name {

    /**
     * Constant <code>NAME_TOKEN_SEPARATOR="."</code>
     */
    String NAME_TOKEN_SEPARATOR = ".";

    @Deprecated
    String NAMESPACE_SEPARATOR = ":";

    Name EMPTY = new EmptyName();

    /**
     * The number of segments of Name produced from given string
     *
     * @param name
     * @return
     */
    static int sizeOf(String name) {
        return Name.of(name).getLength();
    }

    static Name of(String str) {
        if ( str == null || str.isEmpty()) {
            return EMPTY;
        }

        String namespace;
        String name;
        int nsIndex = str.indexOf(NAMESPACE_SEPARATOR);
        if (nsIndex >= 0) {
            namespace = str.substring(0, nsIndex);
            name = str.substring(nsIndex + 1);
        } else {
            namespace = "";
            name = str;
        }

        String[] tokens = name.split("\\.");//TODO исправить возможность появления точки внутри запроса ([^\[\]\.]+(?:\[[^\]]*\])?)*
        if (tokens.length == 1) {
            return new NameToken(namespace, name);
        } else {
            LinkedList<NameToken> list = new LinkedList<>();
            for (String token : tokens) {
                list.add(new NameToken(namespace, token));
            }
            return of(list);
        }
    }

    /**
     * Join all segments in the given order. Segments could be composite.
     *
     * @param segments
     * @return a {@link hep.dataforge.names.Name} object.
     */
    static Name join(String... segments) {
        if (segments.length == 0) {
            return EMPTY;
        } else if (segments.length == 1) {
            return new NameToken("", segments[0]);
        }

        List<NameToken> list = new ArrayList<>();
        for (String segment : segments) {
            if (!segment.isEmpty()) {
                Name segmentName = of(segment);
                if (segmentName instanceof NameToken) {
                    list.add((NameToken) segmentName);
                } else {
                    list.addAll(((NamePath) segmentName).getNames());
                }
            }
        }
        return of(list);
    }

    static String joinString(String... segments) {
        return String.join(NAME_TOKEN_SEPARATOR, segments);
    }

    static Name join(Name... segments) {
        if (segments.length == 0) {
            return EMPTY;
        } else if (segments.length == 1) {
            return segments[0];
        }

        List<NameToken> list = new ArrayList<>();
        for (Name segment : segments) {
            if (segment != EMPTY) {
                if (segment instanceof NameToken) {
                    list.add((NameToken) segment);
                } else {
                    list.addAll(((NamePath) segment).getNames());
                }
            }
        }
        return of(list);
    }

    static Name of(Iterable<String> tokens) {
        return of(StreamSupport.stream(tokens.spliterator(), false)
                .filter(str -> !str.isEmpty())
                .map(token -> new NameToken("", token)).collect(Collectors.toList()));
    }

    static Name of(Collection<NameToken> tokens) {
        if (tokens.size() == 0) {
            return EMPTY;
        } else if (tokens.size() == 1) {
            return tokens.stream().findFirst().get();
        } else {
            LinkedList<NameToken> list = new LinkedList<>();
            list.addAll(tokens);
            return new NamePath(list);
        }
    }


    /**
     * The name as a String including query but ignoring namespace
     *
     * @return
     */
    String nameString();

    /**
     * if has query for the last element
     *
     * @return a boolean.
     */
    boolean hasQuery();

    /**
     * Query for last elements without brackets
     *
     * @return a {@link java.lang.String} object.
     */
    String getQuery();

    /**
     * This name without last element query. If there is no query, returns
     * itself
     *
     * @return
     */
    Name ignoreQuery();

    /**
     * Количество токенов в имени
     *
     * @return a int.
     */
    int getLength();

    /**
     * первый токен
     *
     * @return a {@link hep.dataforge.names.Name} object.
     */
    Name getFirst();

    /**
     * Last token
     *
     * @return a {@link hep.dataforge.names.Name} object.
     */
    Name getLast();

    /**
     * The whole name but the first token
     *
     * @return a {@link hep.dataforge.names.Name} object.
     */
    Name cutFirst();

    /**
     * The whole name but the lat token
     *
     * @return a {@link hep.dataforge.names.Name} object.
     */
    Name cutLast();

    /**
     * The nameSpace of this name. By default is empty
     *
     * @return a {@link java.lang.String} object.
     */
    String nameSpace();

    /**
     * Return the leading name without namespace prefix or query
     *
     * @return a {@link java.lang.String} object.
     */
    String entry();

    /**
     * Convert this name to given namespace. Replace current namespace if
     * exists.
     *
     * @param nameSpace a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.names.Name} object.
     */
    Name toNameSpace(String nameSpace);

    default Name removeNameSpace() {
        return toNameSpace("");
    }

    /**
     * Create a new name with given name appended to the end of this one
     *
     * @param name
     * @return
     */
    default Name append(Name name) {
        return join(this, name);
    }

    default Name append(String name) {
        return join(this, of(name));
    }

    String[] asArray();

    default boolean equals(String name){
        return this.toString().equals(name);
    }
}
