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

import hep.dataforge.description.Described;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.navigation.ValueProvider;
import java.io.Serializable;

/**
 * A convenient container, both named and annotated. Also has methods for
 * descriptor building and built-in value provider.
 *
 * @author Alexander Nozik
 */
public interface Content extends Serializable, Named, Annotated, ValueProvider, Described {

    /**
     * Replaces annotation of current Content or creates a copy of the Content
     * with modified annotation. The resulting annotation does not depend on the
     * former annotation of the Content. So in order to save data in the old
     * annotation use {@code content.setMeta(content.meta().merge(a))}
     *
     * @param a
     * @return
     */
    void setMeta(Meta a);
}
