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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An Identity object allowing to combine many identities into one
 * @author Alexander Nozik
 */
public class CombinedIdentity implements Identity {
    private List<Identity> ids;

    public CombinedIdentity(List<Identity> ids) {
        this.ids = new ArrayList<>(ids);
    }

    public CombinedIdentity(Identity... ids) {
        this.ids = Arrays.asList(ids);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if(this.ids.size() == 1){
            return Objects.equals(obj, ids.get(0));
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CombinedIdentity other = (CombinedIdentity) obj;
        if (!Objects.equals(this.ids, other.ids)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.ids);
        return hash;
    }
    
    
    
}
