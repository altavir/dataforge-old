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

import java.util.Objects;

/**
 * Единичное имя с возможным запросом. На данный момент проверки правильности
 * задания имени при создании не производится
 *
 * @author Alexander Nozik
 */
class NameToken implements Name {

    private final String nameSpace;

    private final String theName;

    private final String theQuery;

    public NameToken(String nameSpace, String singlet) {
        if (singlet.matches(".*\\[.*\\]")) {
            int bracketIndex = singlet.indexOf("[");
            this.theName = singlet.substring(0, bracketIndex);
            this.theQuery = singlet.substring(bracketIndex + 1, singlet.indexOf("]"));
        } else {
            this.theName = singlet;
            this.theQuery = null;
        }

        this.nameSpace = nameSpace;
    }

    @Override
    public Name cutFirst() {
        return Name.EMPTY;
    }

    @Override
    public Name cutLast() {
        return Name.EMPTY;
    }

    @Override
    public Name getFirst() {
        return this;
    }

    @Override
    public Name getLast() {
        return this;
    }

    @Override
    public String getQuery() {
        if (theQuery != null) {
            return theQuery;
        } else {
            return "";
        }
    }

    @Override
    public boolean hasQuery() {
        return theQuery != null;
    }

    @Override
    public NameToken ignoreQuery() {
        if (!hasQuery()) {
            return this;
        } else {
            return new NameToken(nameSpace, theName);
        }
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public String toString() {
        if (nameSpace().isEmpty()) {
            return this.token();
        } else {
            return String.format("%s%s%s", nameSpace(), NAMESPACE_SEPARATOR, token());
        }
    }

    @Override
    public String nameString() {
        return token();
    }

    @Override
    public String entry() {
        return theName;
    }

    @Override
    public String nameSpace() {
        return nameSpace;
    }

    @Override
    public Name toNameSpace(String nameSpace) {
        return new NameToken(nameSpace, token());
    }

    @Override
    public String[] asArray() {
        String[] res = new String[1];
        res[0] = token();
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.nameSpace);
        hash = 79 * hash + Objects.hashCode(this.token());
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
        final NameToken other = (NameToken) obj;
        if (!Objects.equals(this.nameSpace, other.nameSpace)) {
            return false;
        }
        return Objects.equals(token(), other.token());
    }

    /**
     * The full name including query
     */
    private String token() {
        if (theQuery != null) {
            return String.format("%s[%s]", theName, theQuery);
        } else {
            return theName;
        }
    }

}
