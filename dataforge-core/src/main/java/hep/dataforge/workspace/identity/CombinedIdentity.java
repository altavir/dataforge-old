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
import hep.dataforge.meta.MetaBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An Identity object allowing to combine many identities into one
 *
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

        if (this.ids.size() == 1) {
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

    @Override
    public String toString() {
        return "[" + String.join(", ", ids.stream().map(Identity::toString).collect(Collectors.toList())) + "]";
    }

    @Override
    public Meta toMeta() {
        MetaBuilder res =  new MetaBuilder("id")
                .setValue("type","composite");
        this.ids.forEach(id-> res.putNode(id.toMeta()));
        return res;
    }

    @Override
    public void fromMeta(Meta meta) {
        meta.getMetaList("id").forEach(node->ids.add(Identity.from(node)));
    }
}
