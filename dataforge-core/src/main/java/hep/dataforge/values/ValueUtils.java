package hep.dataforge.values;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * Created by darksnake on 06-Aug-16.
 */
public class ValueUtils {

    public static final NumberComparator NUMBER_COMPARATOR = new NumberComparator();

    public static int compare(Value val1, Value val2) {
        switch (val1.valueType()) {
            case NUMBER:
                return NUMBER_COMPARATOR.compare(val1.numberValue(), val2.numberValue());
            case BOOLEAN:
                return Boolean.compare(val1.booleanValue(), val2.booleanValue());
            case STRING:
                return val1.stringValue().compareTo(val2.stringValue());
            case TIME:
                return val1.timeValue().compareTo(val2.timeValue());
            case NULL:
                return val2.valueType() == ValueType.NULL ? 0 : -1;
            default:
                throw new RuntimeException("Uncompareable value");
        }
    }

    /**
     * Checks if given value is between {@code val1} and {@code val1}. Could
     * throw ValueConversionException if value conversion is not possible.
     *
     * @param val1
     * @param val2
     * @return
     */
    public static boolean isBetween(Value val, Value val1, Value val2) {
        return (compare(val, val1) > 0 && compare(val, val2) < 0)
                || (compare(val, val2) > 0 && compare(val, val1) < 0);

    }

    public static boolean isBetween(Object val, Value val1, Value val2) {
        return isBetween(Value.of(val), val1, val2);
    }

    public static class NumberComparator implements Comparator<Number> {

        @Override
        public int compare(final Number x, final Number y) {
            if (isSpecial(x) || isSpecial(y)) {
                return Double.compare(x.doubleValue(), y.doubleValue());
            } else {
                return toBigDecimal(x).compareTo(toBigDecimal(y));
            }
        }

        private static boolean isSpecial(final Number x) {
            boolean specialDouble = x instanceof Double
                    && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
            boolean specialFloat = x instanceof Float
                    && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));
            return specialDouble || specialFloat;
        }

        private static BigDecimal toBigDecimal(final Number number) {
            if (number instanceof BigDecimal) {
                return (BigDecimal) number;
            }
            if (number instanceof BigInteger) {
                return new BigDecimal((BigInteger) number);
            }
            if (number instanceof Byte || number instanceof Short
                    || number instanceof Integer || number instanceof Long) {
                return new BigDecimal(number.longValue());
            }
            if (number instanceof Float || number instanceof Double) {
                return new BigDecimal(number.doubleValue());
            }

            try {
                return new BigDecimal(number.toString());
            } catch (final NumberFormatException e) {
                throw new RuntimeException("The given number (\"" + number
                        + "\" of class " + number.getClass().getName()
                        + ") does not have a parsable string representation", e);
            }
        }
    }

}
