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
package hep.dataforge.content;

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.io.Serializable;

/**
 * A convenient container, both named and annotated. Also has methods for
 * descriptor building and built-in value provider.
 *
 * @author Alexander Nozik
 */
public interface Content extends Serializable, Named, Annotated, ValueProvider {

    /**
     * An unique name of the content
     *
     * @return
     */
    @Override
    String getName();

    /**
     * Replaces annotation of current Content or creates a copy of the Content
     * with modified annotation. The resulting annotation does not depend on the
     * former annotation of the Content. So in order to save data in the old
     * annotation use {@code content.configure(content.meta().merge(a))}
     *
     * @param a
     * @return
     */
    Content configure(Meta a);

    default NodeDescriptor getDescriptor() {
        return DescriptorUtils.buildDescriptor(this.getClass());
    }

    @Override
    Meta meta();

    @Override
    public default Value getValue(String name) {
        if(meta().hasValue(name)){
            return meta().getValue(name);
        } else {
            return DescriptorUtils.buildDefaultNode(getDescriptor()).getValue(name);
        }
    }

    @Override
    public default boolean hasValue(String name) {
        return meta().hasValue(name) || DescriptorUtils.buildDefaultNode(getDescriptor()).hasValue(name);
    }

}
