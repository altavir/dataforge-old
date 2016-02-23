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

import java.util.LinkedList;

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
    public static final String NAME_TOKEN_SEPARATOR = ".";
    
    public static final String NAMESPACE_SEPARATOR = ":";

    public static Name of(String str) {
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
            return new NamePath(list);
        }
    }

    /**
     * Join all segments in the given order. Segments could be composite.
     * @param segments
     * @return a {@link hep.dataforge.names.Name} object.
     */
    public static Name join(String... segments) {
        LinkedList<NameToken> list = new LinkedList<>();
        for (String segment : segments) {
            Name segmentName = of(segment);
            if(segmentName instanceof NameToken){
                list.add((NameToken)segmentName);
            } else {
                list.addAll(((NamePath)segmentName).getNames());
            }
        }
        return new NamePath(list);
    }

    public static Name of(Iterable<String> tokens) {
        LinkedList<NameToken> list = new LinkedList<>();
        for (String token : tokens) {
            list.add(new NameToken("", token));
        }
        return new NamePath(list);
    }

    /**
     *
     * The name as a String including query but ignoring namespace
     *
     * @return
     */
    @Override
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
     * This name without last element query. If there is no query, returns itself
     * @return 
     */
    Name ignoreQuery();
    
    /**
     * Количество токенов в имени
     *
     * @return a int.
     */
    int length();

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
     *  The whole name but the first token
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

    String[] asArray();
}
