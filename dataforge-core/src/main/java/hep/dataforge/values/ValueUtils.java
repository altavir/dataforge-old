package hep.dataforge.values;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by darksnake on 06-Aug-16.
 */
public class ValueUtils {

    public static final Comparator<Number> NUMBER_COMPARATOR = new NumberComparator();
    public static final Comparator<Value> VALUE_COMPARATPR = new ValueComparator();

    public static int compare(Value val1, Value val2) {
        switch (val1.valueType()) {
            case NUMBER:
                return NUMBER_COMPARATOR.compare(val1.numberValue(), val2.numberValue());
            case BOOLEAN:
                return Boolean.compare(val1.booleanValue(), val2.booleanValue());
            case STRING:
                //use alphanumeric comparator here
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


    /**
     * Fast and compact serialization for values
     *
     * @param oos
     * @param value
     * @throws IOException
     */
    public static void writeValue(ObjectOutput oos, Value value) throws IOException {
        if (value.isList()) {
            oos.writeChar('L'); // List designation
            oos.writeShort(value.listValue().size());
            for (Value subValue : value.listValue()) {
                writeValue(oos, subValue);
            }
        } else {
            switch (value.valueType()) {
                case NULL:
                    oos.writeChar('0'); // null
                    break;
                case TIME:
                    oos.writeChar('T');//Instant
                    oos.writeLong(value.timeValue().getEpochSecond());
                    oos.writeLong(value.timeValue().getNano());
                    break;
                case STRING:
                    //TODO add encding specification
                    oos.writeChar('S');//String
                    oos.writeUTF(value.stringValue());
                    break;
                case NUMBER:
                    Number num = value.numberValue();
                    if (num instanceof Double) {
                        oos.writeChar('D'); // double
                        oos.writeDouble(num.doubleValue());
                    } else if (num instanceof Integer) {
                        oos.writeChar('I'); // integer
                        oos.writeInt(num.intValue());
                    } else if (num instanceof BigDecimal) {
                        oos.writeChar('B'); // BigDecimal
                        byte[] bigInt = ((BigDecimal) num).unscaledValue().toByteArray();
                        int scale = ((BigDecimal) num).scale();
                        oos.writeShort(bigInt.length);
                        oos.write(bigInt);
                        oos.writeInt(scale);
                    } else {
                        oos.writeChar('N'); //custom number
                        oos.writeObject(num);
                    }
                    break;
                case BOOLEAN:
                    if (value.booleanValue()) {
                        oos.writeChar('+'); //true
                    } else {
                        oos.writeChar('-'); // false
                    }
                    break;
                default:
                    oos.writeChar('C');//custom
                    oos.writeObject(value);
            }
        }
    }

    /**
     * Value deserialization
     *
     * @param ois
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Value readValue(ObjectInput ois) throws IOException, ClassNotFoundException {
        char c = ois.readChar();
        switch (c) {
            case 'L':
                short listSize = ois.readShort();
                List<Value> valueList = new ArrayList<>();
                for (int i = 0; i < listSize; i++) {
                    valueList.add(readValue(ois));
                }
                return Value.of(valueList);
            case '0':
                return Value.NULL;
            case 'T':
                Instant time = Instant.ofEpochSecond(ois.readLong(), ois.readLong());
                return Value.of(time);
            case 'S':
                return Value.of(ois.readUTF());
            case 'D':
                return Value.of(ois.readDouble());
            case 'I':
                return Value.of(ois.readInt());
            case 'B':
                short intSize = ois.readShort();
                byte[] intBytes = new byte[intSize];
                ois.read(intBytes);
                int scale = ois.readInt();
                BigDecimal bdc = new BigDecimal(new BigInteger(intBytes), scale);
                return Value.of(bdc);
            case 'N':
                return Value.of(ois.readObject());
            case '+':
                return BooleanValue.TRUE;
            case '-':
                return BooleanValue.FALSE;
            case 'C':
                return (Value) ois.readObject();
            default:
                throw new RuntimeException("Wrong value serialization format. Designation " + c + " is unexpected");
        }
    }

    private static class ValueComparator implements Comparator<Value>, Serializable {
        @Override
        public int compare(Value o1, Value o2) {
            return ValueUtils.compare(o1, o2);
        }
    }

    private static class NumberComparator implements Comparator<Number>, Serializable {
        private static final double RELATIVE_NUMERIC_PRECISION = 1e-5;

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

        @Override
        public int compare(final Number x, final Number y) {
            double d1 = x.doubleValue();
            double d2 = y.doubleValue();
            if ((d1 != 0 || d2 != 0) && (Math.abs(d1 - d2) / Math.max(d1, d2) < RELATIVE_NUMERIC_PRECISION)) {
                return 0;
            } else if (isSpecial(x) || isSpecial(y)) {
                return Double.compare(d1, d2);
            } else {
                return toBigDecimal(x).compareTo(toBigDecimal(y));
            }
        }
    }

}
