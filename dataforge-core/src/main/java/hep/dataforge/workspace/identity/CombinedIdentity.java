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
package hep.dataforge.workspace.identity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * An Identity object allowing to combine many identities into one
 * @author Alexander Nozik
 */
public class CombinedIdentity implements Identity {
    private Set<Identity> ids;

    public CombinedIdentity(Collection<Identity> ids) {
        this.ids = new TreeSet<>(ids);
    }

    public CombinedIdentity(Identity... ids) {
        this(Arrays.asList(ids));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if(this.ids.size() == 1){
            return Objects.equals(obj, ids.stream().findFirst().get());
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CombinedIdentity other = (CombinedIdentity) obj;
        return Objects.equals(this.ids, other.ids);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.ids);
        return hash;
    }
    
    
    
}
