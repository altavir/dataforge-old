/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind.extensions

import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import java.time.Instant

/**
 *
 * @author Alexander Nozik
 */
class ValueExtension {
    static Value plus(final Value self, Object obj){
        return plus(self, Value.of(obj))
    }
    
    static Value plus(final Value self, Value other){
        switch(self.valueType()){
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
    
    static Value minus(final Value self, Object obj){
        return minus(self, Value.of(obj))
    }
    
    static Value minus(final Value self, Value other){
        switch(self.valueType()){
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
    
    
    static Value negative(final Value self){
        switch(self.valueType()){
        case ValueType.NUMBER:
            return Value.of(-self.numberValue());
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
    
    static Value multiply(final Value self, Object obj){
        return multiply(self, Value.of(obj))
    }
    
    static Value multiply(final Value self, Value other){
        switch(self.valueType()){
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
            return negate(other);
        }
    }    
    
    static Object asType(final Value self, Class cl){
        if(cl.isAssignableFrom(String.class)){
            return self.stringValue();
        } else if(cl.isAssignableFrom(Number.class)){
            return self.numberValue();
        } else if(cl.isAssignableFrom(Boolean.class)){
            return self.booleanValue();
        } else if(cl.isAssignableFrom(Instant.class)){
            return self.timeValue();
        } else {
            return self;
        }
    }
    
    static equals(final Value self, Object obj){
        if(obj instanceof Number){
            return self.numberValue() == obj
        } else if(obj instanceof String){
            return self.stringValue() == obj
        } else if(obj instanceof Boolean){
            return self.booleanValue() == obj
        } else if(obj instanceof Instant){
            return self.timeValue() == obj
        } else if(obj == null){
            return self.valueType() == ValueType.NULL;
        } else {
            return false
        }
    }
}



