package hep.dataforge.values;

import hep.dataforge.exceptions.ValueConversionException;

import java.time.Instant;

/**
 * Created by darksnake on 05-Aug-16.
 */
public abstract class AbstractValue implements Value {

    /**
     * Smart equality condition
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        //TODO add list values equality condition
        if (obj instanceof Value) {
            Value other = (Value) obj;
            try {
                switch (valueType()) {
                    case BOOLEAN:
                        return this.booleanValue() == other.booleanValue();
                    case TIME:
                        return this.timeValue().equals(other.timeValue());
                    case STRING:
                        return this.stringValue().equals(other.stringValue());
                    case NUMBER:
                        return ValueUtils.NUMBER_COMPARATOR.compare(this.numberValue(), other.numberValue()) == 0;
                    case NULL:
                        return other.valueType() == ValueType.NULL;
                    default:
                        //unreachable statement, but using string comparison just to be sure
                        return this.stringValue().equals(other.stringValue());
                }
            } catch (ValueConversionException ex) {
                return false;
            }
        } else if (obj instanceof Double) {
            return this.doubleValue() == (double) obj;
        } else if (obj instanceof Integer) {
            return this.intValue() == (int) obj;
        } else if (obj instanceof Number) {
            return ValueUtils.NUMBER_COMPARATOR.compare(this.numberValue(), (Number) obj) == 0;
        } else if (obj instanceof String) {
            return this.stringValue().equals(obj);
        } else if (obj instanceof Boolean) {
            return this.booleanValue() == (boolean) obj;
        } else if (obj instanceof Instant) {
            return this.timeValue().equals(obj);
        } else if (obj == null) {
            return this.valueType() == ValueType.NULL;
        } else {
            return super.equals(obj);
        }
    }

    /**
     * Groovy smart cast support
     *
     * @param type
     * @return
     */
    public Object asType(Class type) {
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
