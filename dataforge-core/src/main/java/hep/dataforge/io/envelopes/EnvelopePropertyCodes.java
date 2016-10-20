/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.values.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * An utility class responsible for property-number mapping
 *
 * @author Alexander Nozik
 */
public class EnvelopePropertyCodes {


//    private static final Map<Short, String> charsets = new HashMap<>();
//
//    static {
//        charsets.put((short) 0, "UTF-8");
//        charsets.put((short) 1, "US-ASCII");
//    }
//
//    public static void addCharset(short code, Charset charset) {
//        charsets.put(code, charset.name());
//    }
//
//    /**
//     * Get charset code from name
//     *
//     * @param charsetName
//     * @return
//     */
//    public static short getCharsetCode(String charsetName) {
//        for (Map.Entry<Short, String> entry : charsets.entrySet()) {
//            if (entry.getValue().equalsIgnoreCase(charsetName)) {
//                return entry.getKey();
//            }
//        }
//        throw new RuntimeException("Can not find property code for charset " + charsetName);
//    }
//
//    /**
//     * Get charset code from name
//     *
//     * @param charsetCode
//     * @return
//     */
//    public static String getCharsetName(short charsetCode) {
//        if (charsets.containsKey(charsetCode)) {
//            return charsets.get(charsetCode);
//        } else {
//            throw new PropertyCodeException(charsetCode);
//        }
//    }
//
//    public static Charset getCharset(short code) {
//        return Charset.forName(getCharsetName(code));
//    }
//
//    public static Charset getCharset(String name) {
//        return Charset.forName(name);
//    }
//
//    public static Charset getCharset(Value val) {
//        if (val.valueType() == ValueType.NUMBER) {
//            return getCharset((short) val.intValue());
//        } else {
//            return getCharset(val.stringValue());
//        }
//    }

    private static ServiceLoader<MetaType> metaTypes = ServiceLoader.load(MetaType.class);
    private static Map<String, Map<String, Integer>> mappings = new HashMap<>();

    static {
        invalidate();
        map(Envelope.META_ENCODING_KEY, "UTF-8", 0);
        map(Envelope.META_ENCODING_KEY, "US-ASCII", 1);
        //add additional maeta types for backward compatibility
        map(Envelope.META_TYPE_KEY, "XML", 0);
    }

    /**
     * recalculate mappings
     */
    public static void invalidate() {
        metaTypes.forEach(mt -> map(Envelope.META_TYPE_KEY, mt.getName(), mt.getCode()));
    }

    public static void setMapping(String key, Map<String, Integer> mapping) {
        mappings.put(key, mapping);
    }

    public static void map(String key, String property, int code) {
        Map<String, Integer> mapping = mappings.computeIfAbsent(key, str -> new HashMap<>());
        mapping.put(property, code);
    }

    /**
     * Transform code into value
     *
     * @param property
     * @param code
     * @return
     */
    public static Value decode(String property, int code) {
        if (mappings.containsKey(property)) {
            return mappings.get(property).entrySet().stream()
                    .filter(it -> it.getValue() == code)
                    .map(entry -> Value.of(entry.getKey()))
                    .findAny().orElse(Value.of(code));
        } else {
            return Value.of(code);
        }
    }

    /**
     * Transform value into code
     *
     * @param property
     * @param val
     * @return
     */
    public static int encode(String property, Value val) {
        if (mappings.containsKey(property)) {
            return mappings.get(property).getOrDefault(val.stringValue(), 0x3f3f3f3f);
        } else {
            return val.intValue();
        }
    }

    /**
     * Transform value into short code
     *
     * @param property
     * @param val
     * @return
     */
    public static short encodeShort(String property, Value val) {
        return (short) encode(property, val);
    }

    public static synchronized MetaType getMetaType(String val) {
        for (MetaType mt : metaTypes) {
            if (val.equals(mt.getName())) {
                return mt;
            }
        }
        throw new RuntimeException("Can't find meta type " + val);
    }

    public static MetaType getMetaType(Value name) {
        return getMetaType(name.stringValue());
    }
}
