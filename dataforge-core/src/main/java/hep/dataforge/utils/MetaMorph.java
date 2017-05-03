package hep.dataforge.utils;

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
public interface MetaMorph extends Externalizable {

    static <T extends MetaMorph> T morph(Class<T> type, Meta meta) {
        try {
            T res = type.newInstance();
            res.fromMeta(meta);
            return res;
        } catch (Exception e) {
            throw new Error("Failed to reconstruct metamorph from meta");
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
}
