/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind.extensions

import groovy.transform.CompileStatic
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import hep.dataforge.values.Values

import java.time.Instant

/**
 *
 * @author Alexander Nozik
 */
@CompileStatic
class ValueExtension {

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
                return Value.of( -self.doubleValue());
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
}



