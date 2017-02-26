/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static hep.dataforge.io.envelopes.Wrappable.WRAPPED_TYPE_KEY;

/**
 * Utilities to do simple wrap or unwrap operations
 *
 * @author Alexander Nozik
 */
public class WrapperUtils {

    public static final String JAVA_CLASS_KEY = "javaClass";
    public static final String JAVA_OBJECT_TYPE = "javaObject";

    public static Envelope wrapJavaObject(Serializable obj) {

        EnvelopeBuilder builder = new EnvelopeBuilder();
        builder.setEnvelopeType(new WrapperEnvelopeType());
        builder.putMetaValue(WRAPPED_TYPE_KEY, JAVA_OBJECT_TYPE);
        builder.putMetaValue(JAVA_CLASS_KEY, obj.getClass().getName());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream stream = new ObjectOutputStream(baos)) {
            stream.writeObject(obj);
            builder.setData(baos.toByteArray());
            return builder.build();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
