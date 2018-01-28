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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Единичное имя с возможным запросом. На данный момент проверки правильности
 * задания имени при создании не производится
 *
 * @author Alexander Nozik
 */
class NameToken implements Name {


    private final String theName;

    private final String theQuery;

    public NameToken(String singlet) {
        //unescape string
        singlet = singlet.replace("\\.",".");
        if (singlet.matches(".*\\[.*\\]")) {
            int bracketIndex = singlet.indexOf("[");
            this.theName = singlet.substring(0, bracketIndex);
            this.theQuery = singlet.substring(bracketIndex + 1, singlet.lastIndexOf("]"));
        } else {
            this.theName = singlet;
            this.theQuery = null;
        }
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
            return new NameToken(theName);
        }
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public String toString() {
        return toUnescaped().replace(".", "\\.");
    }

    @Override
    public String entry() {
        return theName;
    }


    @Override
    public String[] asArray() {
        String[] res = new String[1];
        res[0] = toUnescaped();
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.toUnescaped());
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
        return Objects.equals(toUnescaped(), other.toUnescaped());
    }

    /**
     * The full name including query but without escaping
     */
    public String toUnescaped() {
        if (theQuery != null) {
            return String.format("%s[%s]", theName, theQuery);
        } else {
            return theName;
        }
    }

    @Override
    public List<Name> getTokens() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
