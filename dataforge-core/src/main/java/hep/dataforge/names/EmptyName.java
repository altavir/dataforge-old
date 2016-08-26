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

/**
 * Created by darksnake on 26-Aug-16.
 */
class EmptyName implements Name {
    @Override
    public boolean hasQuery() {
        return false;
    }

    @Override
    public String getQuery() {
        return "";
    }

    @Override
    public Name ignoreQuery() {
        return this;
    }

    @Override
    public int length() {
        return 0;
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
    public Name cutFirst() {
        throw new NamingException("Can not cut name token");
    }

    @Override
    public Name cutLast() {
        throw new NamingException("Can not cut name token");
    }

    @Override
    public String nameSpace() {
        return null;
    }

    @Override
    public String entry() {
        return "";
    }

    @Override
    public Name toNameSpace(String nameSpace) {
        return this;
    }

    @Override
    public String[] asArray() {
        return new String[0];
    }

    @Override
    public String toString() {
        return "";
    }
}
