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
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The name path composed of tokens
 *
 * @author Alexander Nozik
 */
class CompositeName implements Name {
    private final LinkedList<NameToken> names;

    @NotNull
    public static CompositeName of(List<Name> tokens){
        LinkedList<NameToken> list = new LinkedList<>();
        tokens.forEach(token -> {
            list.addAll(token.getTokens());
        });
        return new CompositeName(list);
    }

    public CompositeName(LinkedList<NameToken> names) {
        this.names = names;
    }

    @Override
    public Name cutFirst() {
        switch (getLength()) {
            case 2:
                return names.getLast();
            case 1:
                throw new NamingException("Can not cut name token");
            default:
                LinkedList<NameToken> tokens = new LinkedList<>(names);
                tokens.removeFirst();
                return new CompositeName(tokens);
        }
    }

    @Override
    public Name cutLast() {
        switch (getLength()) {
            case 2:
                return names.getFirst();
            case 1:
                throw new NamingException("Can not cut name token");
            default:
                LinkedList<NameToken> tokens = new LinkedList<>(names);
                tokens.removeLast();
                return new CompositeName(tokens);
        }
    }

    @Override
    public Name getFirst() {
        return names.getFirst();
    }

    @Override
    public Name getLast() {
        return names.getLast();
    }

    @Override
    public String getQuery() {
        return names.getLast().getQuery();
    }

    @Override
    public boolean hasQuery() {
        return names.getLast().hasQuery();
    }

    @Override
    public Name ignoreQuery() {
        //Replace last element if needed
        if (hasQuery()) {
            LinkedList<NameToken> tokens = new LinkedList<>(names);
            tokens.removeLast();
            tokens.addLast(names.getLast().ignoreQuery());
            return new CompositeName(tokens);
        } else {
            return this;
        }
    }

    @Override
    public int getLength() {
        return names.size();
    }


    @Override
    public String toString() {
        Iterable<String> it = names.stream().map(NameToken::toString)::iterator;
        return String.join(NAME_TOKEN_SEPARATOR, it);
    }

    @Override
    public String[] asArray() {
        String[] res = new String[getLength()];
        return names.stream().map(NameToken::toString).collect(Collectors.toList()).toArray(res);
    }

    @Override
    public String toUnescaped() {
        Iterable<String> it = names.stream().map(NameToken::toUnescaped)::iterator;
        return String.join(NAME_TOKEN_SEPARATOR, it);
    }

    @Override
    public String entry() {
        return getFirst().entry();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.names);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CompositeName other = (CompositeName) obj;
        return Objects.equals(this.names, other.names);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<NameToken> getTokens() {
        return Collections.unmodifiableList(names);
    }

}
