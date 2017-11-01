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

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * The name path composed of tokens
 *
 * @author Alexander Nozik
 */
class NamePath implements Name {

    private final String nameSpace;
    private final LinkedList<NameToken> names;

    public NamePath(String nameSpace, LinkedList<NameToken> names) {
        this.names = names;
        this.nameSpace = nameSpace;
    }

    public NamePath(LinkedList<NameToken> names) {
        this.names = names;
        this.nameSpace = "";
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
                return new NamePath(tokens);
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
                return new NamePath(tokens);
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
            return new NamePath(tokens);
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
        if (nameSpace().isEmpty()) {
            return nameString();
        } else {
            return String.format("%s%s%s", nameSpace(), NAMESPACE_SEPARATOR, nameString());
        }
    }

    @Override
    public String nameString() {
        Iterable<String> it = names.stream().map((NameToken token) -> token.toString())::iterator;
        return String.join(NAME_TOKEN_SEPARATOR, it);
    }

    @Override
    public String nameSpace() {
        return nameSpace;
    }

    @Override
    public Name toNameSpace(String nameSpace) {
        return new NamePath(nameSpace, names);
    }

    @Override
    public String[] asArray() {
        String[] res = new String[getLength()];
        return names.stream().map((token) -> token.toString()).collect(Collectors.toList()).toArray(res);
    }

    @Override
    public String entry() {
        return getFirst().entry();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.nameSpace);
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
        final NamePath other = (NamePath) obj;
        if (!Objects.equals(this.nameSpace, other.nameSpace)) {
            return false;
        }
        return Objects.equals(this.names, other.names);
    }

    LinkedList<NameToken> getNames() {
        return names;
    }

}
