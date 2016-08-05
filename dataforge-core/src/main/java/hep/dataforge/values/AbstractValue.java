package hep.dataforge.values;

import java.time.Instant;

/**
 * Created by darksnake on 05-Aug-16.
 */
public abstract class AbstractValue implements Value {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Double ) {
            return this.doubleValue() == (double)obj;
        } else if (obj instanceof Integer) {
            return this.intValue() == (int)obj;
        } else if (obj instanceof Number) {
            return this.numberValue() == obj;
        } else if (obj instanceof String) {
            return this.stringValue() == obj;
        } else if (obj instanceof Boolean) {
            return this.booleanValue() == (boolean)obj;
        } else if (obj instanceof Instant) {
            return this.timeValue() == obj;
        } else if (obj == null) {
            return this.valueType() == ValueType.NULL;
        } else {
            return super.equals(obj);
        }

    }

    public Object asType(Class type){
        if (type.isAssignableFrom(String.class)) {
            return this.stringValue();
        } else if (type.isAssignableFrom(Double.class)) {
            return this.doubleValue();
        } else if (type.isAssignableFrom(Integer.class)) {
            return this.intValue();
        } else if (type.isAssignableFrom(Number.class)) {
            return this.numberValue();
        } else if (type.isAssignableFrom(Boolean.class)) {
            return this.booleanValue();
        } else if (type.isAssignableFrom(Instant.class)) {
            return this.timeValue();
        } else {
            return type.cast(this);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
