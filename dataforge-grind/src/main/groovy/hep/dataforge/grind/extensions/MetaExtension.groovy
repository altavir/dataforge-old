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

package hep.dataforge.grind.extensions

import groovy.transform.CompileStatic
import hep.dataforge.grind.GrindMetaBuilder
import hep.dataforge.grind.GrindUtils
import hep.dataforge.meta.*
import hep.dataforge.values.MapValueProvider
import hep.dataforge.values.NamedValue
import hep.dataforge.values.Value

/**
 * Created by darksnake on 20-Aug-16.
 */
@CompileStatic
class MetaExtension {
    static MetaBuilder plus(final Meta self, MetaBuilder other) {
        return new JoinRule().merge(self, other);
    }

    static MetaBuilder plus(final Meta self, NamedValue other) {
        return new MetaBuilder(self).putValue(other.getName(), other);
    }

    /**
     * Put a mixed map of nodes and values into new meta based on existing one
     * @param self
     * @param map
     * @return
     */
    static MetaBuilder plus(final Meta self, Map<String, Object> map) {
        MetaBuilder res = new MetaBuilder(self);
        map.forEach { String key, value ->
            if (value instanceof Meta) {
                res.putNode(key, value);
            } else {
                res.putValue(key, value)
            }
        }
        return res;
    }

    static MetaBuilder leftShift(final MetaBuilder self, MetaBuilder other) {
        return new MetaBuilder(self).putNode(other);
    }

    static MetaBuilder leftShift(final MetaBuilder self, NamedValue other) {
        return new MetaBuilder(self).putValue(other.getName(), other);
    }

    static MetaBuilder leftShift(final MetaBuilder self, Map<String, Object> map) {
        map.forEach { String key, value ->
            if (value instanceof Meta) {
                self.putNode(key, value);
            } else {
                self.putValue(key, value)
            }
        }
        return self;
    }

    /**
     * Update existing builder using closure
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder update(final MetaBuilder self, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return self.update(GrindUtils.buildMeta(cl))
    }

    /**
     * Update existing builder using map of values and closure
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder update(final MetaBuilder self, Map values, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return self.update(GrindUtils.buildMeta(values, cl))
    }

    /**
     * Create a new builder and update it from closure (existing one not changed)
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder transform(final Meta self, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return new MetaBuilder(self).update(GrindUtils.buildMeta(self.getName(), cl))
    }

    /**
     * Create a new builder and update it from closure and value map (existing one not changed)
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder transform(final Meta self, Map values, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return new MetaBuilder(self).update(GrindUtils.buildMeta(self.getName(), values, cl))
    }

    /**
     * Create a new builder and update it from map (existing one not changed)
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder transform(final Meta self, Map values) {
        return new MetaBuilder(self).update(values)
    }

    //TODO add tests
    static void setProperty(final MetaBuilder self, String name, Object value) {
        self.setValue(name, value)
    }

    static Value getProperty(final Meta self, String name) {
        return self.getValue(name)
    }

    static Value getAt(final Meta self, String name){
        return self.getValue(name);
    }

    static void setAt(final MetaBuilder self, String name, Object value){
        self.setValue(name, value)
    }

    /**
     * Compile new builder using self as a template
     * @param self
     * @param dataSource
     * @return
     */
    static MetaBuilder compile(final Meta self, Meta dataSource) {
        return Template.compileTemplate(self, dataSource);
    }

    static MetaBuilder compile(final Meta self, Map<String, Object> dataMap) {
        return Template.compileTemplate(self, dataMap);
    }

    /**
     * Use map as a value provider and given meta as meta provider
     * @param template
     * @param map
     * @param cl
     * @return
     */
    static MetaBuilder compile(final Meta self, Map<String, Object> map, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        Template tmp = new Template(self);
        return tmp.compile(new MapValueProvider(map), GrindUtils.buildMeta(cl));
    }

    static Configurable configure(final Configurable self, Closure configuration) {
        self.configure(GrindUtils.buildMeta("config", configuration));
    }

    static Configurable configure(final Configurable self, Map values, Closure configuration) {
        self.configure(GrindUtils.buildMeta("config", values, configuration));
    }

    static Configurable setAt(final Configurable self, String key, Object value) {
        self.configureValue(key, value);
    }

}
