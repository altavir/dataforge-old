package hep.dataforge.utils;

import hep.dataforge.data.AutoCastable;
import hep.dataforge.exceptions.AutoCastException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Ab object that could be represented as meta. Serialized via meta serializer and deserialized back
 * Created by darksnake on 12-Nov-16.
 */
public interface MetaMorph extends Externalizable, AutoCastable {

    /**
     *
     * @param type
     * @param meta
     * @param <T>
     * @return
     */
    static <T extends MetaMorph> T morph(Class<T> type, Meta meta) {
        try {
            T res = type.getConstructor().newInstance();
            res.fromMeta(meta);
            return res;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct metamorph from meta", e);
        }
    }

    /**
     * Create a metamorph from input stream using its own tag
     * @param in
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    static MetaMorph morph(ObjectInput in) throws Exception {
        String tag = in.readUTF();
        Class<? extends MetaMorph> type = (Class<? extends MetaMorph>) Class.forName(tag);
        return morph(type,MetaUtils.readMeta(in));
    }

    /**
     * Convert this object to Meta
     *
     * @return
     */
    Meta toMeta();

    /**
     * Reconstruct this object form Meta. The object must be blank, otherwise {@link IllegalStateException} is thrown
     *
     * @param meta
     */
    void fromMeta(Meta meta);
    
    default String getMetaMorphTag(){
        return getClass().getName();
    }

    @Override
    default void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(getMetaMorphTag());
        MetaUtils.writeMeta(out, toMeta());
    }

    @Override
    default void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String tag = in.readUTF();
        Class<?> type = Class.forName(tag);
        if(!getClass().isAssignableFrom(type)){
            throw new RuntimeException("Trying to reconstruct metamorph from wrong source");
        }
        fromMeta(MetaUtils.readMeta(in));
    }

    /**
     * Auto converts MetaMorph to Meta.
     * Tries to convert one metamorph into another by first converting to meta and then converting back.
     * If the conversion is failed, catch the exception and rethrow it as {@link AutoCastException}
     *
     * @param type
     * @param <T>
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    default <T> T cast(Class<T> type) {
        if (type == Meta.class) {
            return (T) toMeta();
        } else if (MetaMorph.class.isAssignableFrom(type)) {
            try {
                return toMeta().cast(type);
            } catch (Exception ex) {
                throw new AutoCastException("", getClass(), type, ex);
            }
        } else {
            return AutoCastable.super.cast(type);
        }
    }
}
