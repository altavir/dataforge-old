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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
public interface Name extends Comparable<Name> {

    /**
     * Constant <code>NAME_TOKEN_SEPARATOR="."</code>
     */
    String NAME_TOKEN_SEPARATOR = ".";


    Name EMPTY = new EmptyName();

    static Name empty() {
        return EMPTY;
    }

    /**
     * The number of segments of Name produced from given string
     *
     * @param name
     * @return
     */
    static int lengthOf(String name) {
        return Name.of(name).getLength();
    }

    static Name of(String name) {
        if (name == null || name.isEmpty()) {
            return EMPTY;
        }
        String[] tokens = name.split("(?<!\\\\)\\.");
        if (tokens.length == 1) {
            return new NameToken(name);
        } else {
            return of(Stream.of(tokens).map(NameToken::new).collect(Collectors.toList()));
        }
    }

    /**
     * Build name from string ignoring name token separators and treating it as a single name token
     *
     * @param name
     * @return
     */
    static Name ofSingle(String name) {
        if (name.isEmpty()) {
            return EMPTY;
        } else {
            return new NameToken(name);
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
            return of(segments[0]);
        }

        return of(Stream.of(segments).filter(it -> !it.isEmpty()).map(Name::of).collect(Collectors.toList()));
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

        return of(Stream.of(segments).filter(it -> !it.isEmpty()).collect(Collectors.toList()));
    }

    static Name of(Iterable<String> tokens) {
        return of(StreamSupport.stream(tokens.spliterator(), false)
                .filter(str -> !str.isEmpty())
                .map(NameToken::new).collect(Collectors.toList()));
    }

    static Name of(List<Name> tokens) {
        if (tokens.size() == 0) {
            return EMPTY;
        } else if (tokens.size() == 1) {
            return tokens.get(0);
        } else {
            return CompositeName.of(tokens);
        }
    }


    /**
     * The name as a String including query but ignoring namespace
     *
     * @return
     */
    String toString();

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
     * Return the leading name without query
     *
     * @return a {@link java.lang.String} object.
     */
    String entry();

    /**
     * Get the list of contained tokens
     *
     * @return
     */
    List<NameToken> getTokens();

    /**
     * Returns true only for EMPTY name
     *
     * @return
     */
    boolean isEmpty();

    /**
     * Create a new name with given name appended to the end of this one
     *
     * @param name
     * @return
     */
    default Name append(Name name) {
        return join(this, name);
    }

    /**
     * Append a name to the end of this name treating new name as a single name segment
     *
     * @param name
     * @return
     */
    default Name append(String name) {
        return join(this, ofSingle(name));
    }

    String[] asArray();

    default boolean equals(String name) {
        return this.toString().equals(name);
    }

    @Override
    default int compareTo(@NotNull Name o) {
        return this.toString().compareTo(o.toString());
    }

    /**
     * Convert to string without escaping separators
     *
     * @return
     */
    String toUnescaped();
}
