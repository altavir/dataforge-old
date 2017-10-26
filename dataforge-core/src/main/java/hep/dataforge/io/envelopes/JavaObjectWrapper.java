/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Alexander Nozik
 */
public class JavaObjectWrapper implements Wrapper<Object> {

    public static final String JAVA_CLASS_KEY = "javaClass";
    public static final String JAVA_OBJECT_TYPE = "df.object";

    @Override
    public String getName() {
        return JAVA_OBJECT_TYPE;
    }


    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    @Override
    public Envelope wrap(Object obj) {
        EnvelopeBuilder builder = new EnvelopeBuilder()
                .setContentType("wrapper")
                .putMetaValue(WRAPPER_TYPE_KEY, JAVA_OBJECT_TYPE)
                .putMetaValue(JAVA_CLASS_KEY, obj.getClass().getName());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream stream = new ObjectOutputStream(baos)) {
            stream.writeObject(obj);
            builder.setData(baos.toByteArray());
            return builder.build();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object unWrap(Envelope envelope) {
        if (!getName().equals(envelope.meta().getString(WRAPPER_TYPE_KEY, ""))) {
            throw new Error("Wrong wrapped type: " + envelope.meta().getString(WRAPPER_TYPE_KEY, ""));
        }
        try {
            ObjectInputStream stream = new ObjectInputStream(envelope.getData().getStream());
            return stream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
