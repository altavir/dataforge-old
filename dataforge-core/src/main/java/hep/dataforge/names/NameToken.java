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
import java.util.Objects;

/**
 * Единичное имя с возможным запросом. На данный момент проверки правильности
 * задания имени при создании не производится
 *
 * @author Alexander Nozik
 */
class NameToken implements Name {

    private final String nameSpace;

    private final String singlet;

    public NameToken(String nameSpace, String singlet) {
        this.singlet = singlet;
        this.nameSpace = nameSpace;
    }

    @Override
    public Name cutFirst() {
        throw new NamingException("Can not cut name token");
    }

    @Override
    public Name cutLast() {
        throw new NamingException("Can not cut name token");
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
        if (hasQuery()) {
            return singlet.substring(singlet.indexOf("[")+1,singlet.indexOf("]"));
        } else {
            return "";
        }
    }

    @Override
    public boolean hasQuery() {
        return singlet.matches(".*\\[.*\\]");
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public String toString() {
        if (nameSpace().isEmpty()) {
            return this.singlet;
        } else {
            return String.format("%s%s%s", nameSpace(),NAMESPACE_SEPARATOR,singlet);
        }
    }

    @Override
    public String entry() {
        if(hasQuery()){
            return singlet.substring(0, singlet.indexOf("["));
        } else{
            return singlet;
        }
    }

    @Override
    public String nameSpace() {
        return nameSpace;
    }

    @Override
    public Name toNameSpace(String nameSpace) {
        return new NameToken(nameSpace, singlet);
    }

    @Override
    public String[] asArray() {
        String[] res = new String[1];
        res[0] = singlet;
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.nameSpace);
        hash = 79 * hash + Objects.hashCode(this.singlet);
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
        if (!Objects.equals(this.singlet, other.singlet)) {
            return false;
        }
        return true;
    }

    
    
}
