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

import hep.dataforge.meta.Meta;

import java.io.Serializable;

/**
 * A marker interface that designates some object with could call {@code equals}
 * on another Identity and check if both correspond to the same state. Identity
 * objects does not have to be precisely identical.
 *
 * @author Alexander Nozik
 */
public interface Identity extends Serializable, Comparable<Identity> {

    /**
     * The string representation of this identity. Usually a hash code
     *
     * @return
     */
    @Override
    String toString();
    
    /**
     * Return a joined identity.
     * @param ids
     * @return 
     */
    default Identity and(Identity id){
        return new CombinedIdentity(this, id);
    }
    
    default Identity and(String str){
        return and(new StringIdentity(str));
    }
    
    default Identity and(Meta meta){
        return and(new MetaIdentity(meta));
    }    

    @Override
    default int compareTo(Identity o) {
        return Integer.compare(this.hashCode(), o.hashCode());
    }
    
    

}
