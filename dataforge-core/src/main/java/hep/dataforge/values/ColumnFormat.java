/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.values;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Special format for columned text
 *
 * @author Alexander Nozik
 */
public abstract class ColumnFormat implements ValueFormatter {

    private DecimalFormat expFormat;
    private DecimalFormat plainFormat;

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

    protected DecimalFormat getExpFormat() {
        return new DecimalFormat(String.format("0.%sE0#;(-0.%sE0#)", grids(getMaxWidth() - 6), grids(getMaxWidth() - 7)));
    }

    protected final String grids(int num) {
        if (num <= 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < num; i++) {
            b.append("#");
        }
        return b.toString();
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

        if (bd.precision() - bd.scale() > 2 - getMaxWidth()) {
            if (number instanceof Integer) {
                return String.format("%d", number);
            } else {
                return String.format("%." + (maxWidth - 1) + "g", bd.stripTrailingZeros());
            }
            //return getFlatFormat().format(bd);
        } else {
            return getExpFormat().format(bd);
        }
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
                return formatWidth(formatNumber(val.numberValue()));
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
