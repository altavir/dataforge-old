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
package hep.dataforge.workspace;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaNode;
import java.util.Objects;

public class MetaIdentity implements Identity {

    private final Meta meta;

    /**
     * Constructor snapshots the meta state when it is created, if meta is
     * somehow changed later, identity still remembers old state.
     *
     * @param meta
     */
    public MetaIdentity(Meta meta) {
        this.meta = MetaNode.from(meta);
    }

    @Override
    public int hashCode() {
        int hash = 3 + meta.hashCode();
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
        final MetaIdentity other = (MetaIdentity) obj;
        if (!Objects.equals(this.meta, other.meta)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "meta::"+hashCode();
    }
    
    

}
