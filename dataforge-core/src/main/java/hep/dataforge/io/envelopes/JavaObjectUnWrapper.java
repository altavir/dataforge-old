/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import static hep.dataforge.io.envelopes.Wrappable.WRAPPED_TYPE_KEY;
import static hep.dataforge.io.envelopes.WrapperUtils.JAVA_OBJECT_TYPE;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author Alexander Nozik
 */
public class JavaObjectUnWrapper implements UnWrapper<Object> {

    @Override
    public String type() {
        return JAVA_OBJECT_TYPE;
    }

    @Override
    public Object unWrap(Envelope envelope) {
        if (!type().equals(envelope.meta().getString(WRAPPED_TYPE_KEY, ""))) {
            throw new Error("Wrong wrapped type: " + envelope.meta().getString(WRAPPED_TYPE_KEY, ""));
        }
        try {
            ObjectInputStream stream = new ObjectInputStream(envelope.getData().getStream());
            return stream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
