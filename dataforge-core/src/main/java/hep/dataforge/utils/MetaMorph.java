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

    static <T extends MetaMorph> T morph(Class<T> type, Meta meta) {
        try {
            T res = type.newInstance();
            res.fromMeta(meta);
            return res;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct metamorph from meta", e);
        }
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

    @Override
    default void writeExternal(ObjectOutput out) throws IOException {
        //TODO add type tag to avoid reconstructing metamorph from meta saved form another type
        MetaUtils.writeMeta(out, toMeta());
    }

    @Override
    default void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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
    default <T> T asType(Class<T> type) {
        if (type == Meta.class) {
            return (T) toMeta();
        } else if (MetaMorph.class.isAssignableFrom(type)) {
            try {
                return toMeta().asType(type);
            } catch (Exception ex) {
                throw new AutoCastException("", getClass(), type, ex);
            }
        } else {
            return AutoCastable.super.asType(type);
        }
    }
}
