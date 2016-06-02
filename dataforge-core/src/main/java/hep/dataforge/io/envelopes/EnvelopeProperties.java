/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.exceptions.PropertyCodeException;
import hep.dataforge.values.CompositePropertyValue;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 *
 * @author Alexander Nozik
 */
public class EnvelopeProperties {

    public static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");

    private static final ServiceLoader<MetaType> loader = ServiceLoader.load(MetaType.class);

    private static final Set<Value> charsets = new HashSet<>();

    static {
        charsets.add(new CompositePropertyValue(0, "UTF-8"));
        charsets.add(new CompositePropertyValue(1, "US-ASCII"));
    }

    public static void addCharset(int code, Charset charset) {
        charsets.add(new CompositePropertyValue(code, charset.name()));
    }

    public static Value getCharsetValue(String charsetName) {
        for (Value val : charsets) {
            if (val.stringValue().equalsIgnoreCase(charsetName)) {
                return val;
            }
        }
        return null;
    }

    public static Value getCharsetValue(short charsetCode) {
        for (Value val : charsets) {
            if (val.intValue() == charsetCode) {
                return val;
            }
        }
        throw new PropertyCodeException(charsetCode);
    }

    public static Charset getCharset(short code) {
        return getCharset(getCharsetValue(code).stringValue());
    }

    public static Charset getCharset(String name) {
        return Charset.forName(name);
    }

    public static Charset getCharset(Value val) {
        if (val.valueType() == ValueType.NUMBER) {
            return getCharset((short) val.intValue());
        } else {
            return getCharset(val.stringValue());
        }
    }

    public static MetaType getMetaType(String name) {
        return EnvelopeProperties.getMetaType(Value.of(name));
    }

    public static MetaType getMetaType(short code) {
        return EnvelopeProperties.getMetaType(Value.of(code));
    }

    public static synchronized MetaType getMetaType(Value val) {
        for (MetaType mt : loader) {
            if (val.equals(mt.getValue())) {
                return mt;
            }
        }
        throw new PropertyCodeException(val.stringValue());
    }
}
