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
import hep.dataforge.data.DataNode
import hep.dataforge.grind.Grind
import hep.dataforge.grind.GrindMetaBuilder
import hep.dataforge.meta.*
import hep.dataforge.tables.Table
import hep.dataforge.values.*
import hep.dataforge.workspace.Workspace

import java.time.Instant

/**
 * Created by darksnake on 20-Aug-16.
 */
@CompileStatic
class CoreExtension {

    //value extensions

    static Value plus(final Value self, Object obj) {
        return plus(self, Value.of(obj))
    }

    static Value plus(final Value self, Value other) {
        switch (self.getType()) {
            case ValueType.NUMBER:
                return Value.of(self.numberValue() + other.numberValue());
            case ValueType.STRING:
                return Value.of(self.stringValue() + other.stringValue());
            case ValueType.TIME:
                //TODO implement
                throw new RuntimeException("Time plus operator is not yet supported")
            case ValueType.BOOLEAN:
                //TODO implement
                throw new RuntimeException("Boolean plus operator is not yet supported")
            case ValueType.NULL:
                return other;
        }
    }

    static Value minus(final Value self, Object obj) {
        return minus(self, Value.of(obj))
    }

    static Value minus(final Value self, Value other) {
        switch (self.getType()) {
            case ValueType.NUMBER:
                return Value.of(self.numberValue() - other.numberValue());
            case ValueType.STRING:
                return Value.of(self.stringValue() - other.stringValue());
            case ValueType.TIME:
                //TODO implement
                throw new RuntimeException("Time plus operator is not yet supported")
            case ValueType.BOOLEAN:
                //TODO implement
                throw new RuntimeException("Boolean plus operator is not yet supported")
            case ValueType.NULL:
                return negative(other);
        }
    }


    static Value negative(final Value self) {
        switch (self.getType()) {
            case ValueType.NUMBER:
                //TODO fix non-dobule values
                return Value.of(-self.doubleValue());
            case ValueType.STRING:
                throw new RuntimeException("Can't negate String value")
            case ValueType.TIME:
                throw new RuntimeException("Can't negate time value")
            case ValueType.BOOLEAN:
                return Value.of(!self.booleanValue());
            case ValueType.NULL:
                return self;
        }
    }

    static Value multiply(final Value self, Object obj) {
        return multiply(self, Value.of(obj))
    }

    static Value multiply(final Value self, Value other) {
        switch (self.getType()) {
            case ValueType.NUMBER:
                return Value.of(self.numberValue() * other.numberValue());
            case ValueType.STRING:
                return Value.of(self.stringValue() * other.intValue());
            case ValueType.TIME:
                //TODO implement
                throw new RuntimeException("Time multiply operator is not yet supported")
            case ValueType.BOOLEAN:
                //TODO implement
                throw new RuntimeException("Boolean multiply operator is not yet supported")
            case ValueType.NULL:
                return Value.NULL;
        }
    }

    static Object asType(final Value self, Class type) {
        switch (type) {
            case double:
                return self.doubleValue();
            case int:
                return self.intValue();
            case short:
                return self.numberValue().shortValue();
            case long:
                return self.numberValue().longValue();
            case Number:
                return self.numberValue();
            case String:
                return self.stringValue();
            case boolean:
                return self.booleanValue();
            case Instant:
                return self.timeValue();
            case Date:
                return Date.from(self.timeValue());
            default:
                throw new RuntimeException("Unknown value cast type: ${type}");
        }
    }

//    /**
//     * Unwrap value and return its content in its native form. Possible loss of precision for numbers
//     * @param self
//     * @return
//     */
//    static Object unbox(final Value self) {
//        switch (self.getType()) {
//            case ValueType.NUMBER:
//                return self.doubleValue();
//            case ValueType.STRING:
//                return self.stringValue();
//            case ValueType.TIME:
//                return self.timeValue();
//            case ValueType.BOOLEAN:
//                return self.booleanValue();
//            case ValueType.NULL:
//                return null;
//        }
//    }

    /**
     * Represent DataPoint as a map of typed objects according to value type
     * @param self
     * @return
     */
    static Map<String, Object> unbox(final Values self) {
        self.getNames().collectEntries {
            [it: self.getValue(it).value()]
        }
    }

    /**
     * Groovy extension to access DataPoint fields
     * @param self
     * @param field
     * @return
     */
    static Value getAt(final Values self, String field) {
        return self.getValue(field);
    }

    static Value getProperty(final Values self, String name) {
        return self.getValue(name)
    }

    //meta extensions

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
        return self.update(Grind.buildMeta(cl))
    }

    /**
     * Update existing builder using map of values and closure
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder update(final MetaBuilder self, Map values, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return self.update(Grind.buildMeta(values, cl))
    }

    /**
     * Create a new builder and update it from closure (existing one not changed)
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder transform(final Meta self, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return new MetaBuilder(self).update(Grind.buildMeta(self.getName(), cl))
    }

    /**
     * Create a new builder and update it from closure and value map (existing one not changed)
     * @param self
     * @param cl
     * @return
     */
    static MetaBuilder transform(final Meta self, Map values, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return new MetaBuilder(self).update(Grind.buildMeta(values, self.getName(), cl))
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

    static Object getAt(final Meta self, String name) {
        return self.getValue(name).value();
    }

    static void setAt(final MetaBuilder self, String name, Object value) {
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
    static MetaBuilder compile(final Meta self, Map<String, ?> map, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        Template tmp = new Template(self);
        return tmp.compile(new MapValueProvider(map), Grind.buildMeta(cl));
    }

    static Configurable configure(final Configurable self, Closure configuration) {
        self.configure(Grind.buildMeta("config", configuration));
    }

    static Configurable configure(final Configurable self, Map<String, ?> values, Closure configuration) {
        self.configure(Grind.buildMeta(values, "config", configuration));
    }

    static Configurable configure(final Configurable self, Map<String, ?> values) {
        self.configure(Grind.buildMeta(values, "config"));
    }

    static Configurable setAt(final Configurable self, String key, Object value) {
        self.configureValue(key, value);
    }

    //table extension
    static Values getAt(final Table self, int index) {
        return self.getRow(index);
    }

    static Object getAt(final Table self, String name, int index) {
        return self.get(name, index).value();
    }

    //workspace extension
    static DataNode run(final Workspace wsp, String command) {
        if (command.contains("(") || command.contains("{")) {
            Meta meta = Grind.parseMeta(command);
            return wsp.runTask(meta);
        } else {
            return wsp.runTask(command)
        }
    }
}