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
package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.meta.MetaBuilder

/**
 * A builder to create annotations
 * @author Alexander Nozik
 */
@CompileStatic
class GrindMetaBuilder extends BuilderSupport {
    @Override
    MetaBuilder createNode(Object name) {
        return createNode(name, [:]);
    }

    @Override
    MetaBuilder createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }

    private boolean isCollectionOrArray(Object object) {
        return object instanceof Collection || object.getClass().isArray()
    }

    @Override
    MetaBuilder createNode(Object name, Map attributes, Object value) {
        MetaBuilder res = new MetaBuilder(name.toString());
        attributes.each { k, v ->
            if (isCollectionOrArray(v)) {
                v.each {
                    res.putValue(k.toString(), it);
                }
            } else {
                res.putValue(k.toString(), v);
            }
        }
        if (value != null && value instanceof MetaBuilder) {
            res.putNode((MetaBuilder) value);
        }
        return res;
    }

    @Override
    MetaBuilder createNode(Object name, Object value) {
        MetaBuilder res = new MetaBuilder(name.toString());
        if (value != null && value instanceof MetaBuilder) {
            res.putNode((MetaBuilder) value);
        }
        return res;
    }

    @Override
    void setParent(Object parent, Object child) {
        ((MetaBuilder) parent).attachNode((MetaBuilder) child);
    }
}

