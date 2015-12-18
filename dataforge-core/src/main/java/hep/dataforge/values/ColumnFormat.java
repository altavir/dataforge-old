/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.values;

import java.math.BigDecimal;

/**
 *
 * @author Alexander Nozik
 */
public abstract class ColumnFormat implements ValueFormat {

    /**
     * Get maximum width of column in symbols
     *
     * @return
     */
    public abstract int getMaxWidth();

    /**
     * The default type of this column. Theoretically it is possible to use
     * other types is well.
     *
     * @return
     */
    public abstract ValueType primaryType();

    private String formatWidth(String val) {
        return String.format("%" + getMaxWidth() + "s", val);
    }

    private String formatNumber(Number number) {
        BigDecimal bd;
        if (number instanceof BigDecimal) {
            bd = (BigDecimal) number;
        } else if (number instanceof Integer) {
            bd = BigDecimal.valueOf(number.intValue());
        } else {
            bd = BigDecimal.valueOf(number.doubleValue());
        }
        int maxWidth = getMaxWidth();
        if (bd.doubleValue() == 0) {
            return String.format("%" + maxWidth + "s", "0");
        }
//
//        if (bd.doubleValue() != 0) {
//            bd = bd.round(new MathContext(maxWidth, RoundingMode.HALF_DOWN)).stripTrailingZeros();
//        } else {
//            return String.format("%" + maxWidth +"s", "0");
//        }
//        
//        int precision = maxWidth;
//
//        if (bd.signum() < 0) {
//            precision--;//sign
//        }
//
//        if (bd.scale() != 0) {
//            precision--;//decimal point
//        }
//
//        if ((bd.scale() - bd.precision() >= maxWidth - 2) || bd.scale() < 0) {
//            precision -= 4;
//        }
//
//        if (precision <= 0) {
//            precision = 0;
//        }
//        
        int precision = Math.min(maxWidth, significantDigits(bd));

        //FIXME fix number formats!!!
        return String.format("%" + maxWidth + "." + precision + "g", bd);

    }

    private int significantDigits(BigDecimal input) {
        return input.scale() <= 0
                ? input.precision() + input.scale()
                : input.precision();
    }

    @Override
    public String format(Value val) {
        switch (val.valueType()) {
            case BOOLEAN:
                if (getMaxWidth() >= 5) {
                    return Boolean.toString(val.booleanValue());
                } else if (val.booleanValue()) {
                    return formatWidth("+");
                } else {
                    return formatWidth("-");
                }
            case NULL:
                return formatWidth("");
            case NUMBER:
                return formatNumber(val.numberValue());
            case STRING:
                return formatWidth(val.stringValue());
            case TIME:
                //TODO add time shortening
                return formatWidth(val.stringValue());
            default:
                throw new IllegalArgumentException("Unsupported input value type");
        }
    }

}
