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
                switch (getType()) {
                    case BOOLEAN:
                        return this.getBoolean() == other.getBoolean();
                    case TIME:
                        return this.getTime().equals(other.getTime());
                    case STRING:
                        return this.getString().equals(other.getString());
                    case NUMBER:
                        return ValueUtils.NUMBER_COMPARATOR.compare(this.getNumber(), other.getNumber()) == 0;
                    case NULL:
                        return other.getType() == ValueType.NULL;
                    default:
                        //unreachable statement, but using string comparison just to be sure
                        return this.getString().equals(other.getString());
                }
            } catch (ValueConversionException ex) {
                return false;
            }
        } else if (obj instanceof Double) {
            return this.getDouble() == (double) obj;
        } else if (obj instanceof Integer) {
            return this.getInt() == (int) obj;
        } else if (obj instanceof Number) {
            return ValueUtils.NUMBER_COMPARATOR.compare(this.getNumber(), (Number) obj) == 0;
        } else if (obj instanceof String) {
            return this.getString().equals(obj);
        } else if (obj instanceof Boolean) {
            return this.getBoolean() == (boolean) obj;
        } else if (obj instanceof Instant) {
            return this.getTime().equals(obj);
        } else if (obj == null) {
            return this.getType() == ValueType.NULL;
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
    public Object asType(Class<?> type) {
        if (type.isAssignableFrom(String.class)) {
            return this.getString();
        } else if (type.isAssignableFrom(Double.class)) {
            return this.getDouble();
        } else if (type.isAssignableFrom(Integer.class)) {
            return this.getInt();
        } else if (type.isAssignableFrom(Number.class)) {
            return this.getNumber();
        } else if (type.isAssignableFrom(Boolean.class)) {
            return this.getBoolean();
        } else if (type.isAssignableFrom(Instant.class)) {
            return this.getTime();
        } else {
            return type.cast(this);
        }
    }

    @Override
    public String toString() {
        return getString();
    }
}
