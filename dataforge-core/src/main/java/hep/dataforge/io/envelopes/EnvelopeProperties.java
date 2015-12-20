/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.exceptions.PropertyCodeException;
import hep.dataforge.values.CompositePropertyValue;
import hep.dataforge.values.Value;
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

    public static void addCharset(int code, Charset charset){
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
        return getCharset(val.stringValue());
    }

    public static MetaType getType(String name) {
        return getType(Value.of(name));
    }

    public static MetaType getType(short code) {
        return getType(Value.of(code));
    }

    public static MetaType getType(Value val) {
        for (MetaType mt : loader) {
            if (mt.getValue().equals(val)) {
                return mt;
            }
        }
        throw new PropertyCodeException(val.stringValue());
    }
}
