/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.values;

import java.math.BigDecimal;

/**
 * Special format for columned text
 *
 * @author Alexander Nozik
 */
public abstract class ColumnFormat implements ValueFormatter {

    /**
     * Get maximum width of column in symbols
     *
     * @return
     */
    public abstract int getMaxWidth();

    /**
     * The default type of this column. Theoretically it is possible to use
     * other types as well.
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
                return formatWidth("@null");
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
