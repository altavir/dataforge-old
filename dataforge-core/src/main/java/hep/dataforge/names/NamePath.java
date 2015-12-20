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
 * Путь, состоящий из нескольких токенов
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
        if (length() == 2) {
            return names.getLast();
        } else if (length() == 1) {
            throw new NamingException("Can not cut name token");
        } else {
            LinkedList<NameToken> tokens = new LinkedList<>(names);
            tokens.removeFirst();
            return new NamePath(tokens);
        }
    }

    @Override
    public Name cutLast() {
        if (length() == 2) {
            return names.getFirst();
        } else if (length() == 1) {
            throw new NamingException("Can not cut name token");
        } else {
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
    public int length() {
        return names.size();
    }

    @Override
    public String toString() {
        Iterable<String> it = names.stream().map((NameToken token) -> token.toString())::iterator;
        String sum = String.join(".", it);
        if (nameSpace().isEmpty()) {
            return sum;
        } else {
            return String.format("%s%s%s", nameSpace(), NAMESPACE_SEPARATOR, sum);
        }
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
        String[] res = new String[length()];
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
        if (!Objects.equals(this.names, other.names)) {
            return false;
        }
        return true;
    }

    LinkedList<NameToken> getNames() {
        return names;
    }

}