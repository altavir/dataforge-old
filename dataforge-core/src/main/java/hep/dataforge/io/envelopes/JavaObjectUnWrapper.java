/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

/**
 *
 * @author Alexander Nozik
 */
public class JavaObjectUnWrapper implements UnWrapper<Object>{
    public static final String JAVA_OBJECT_TYPE = "javaObject";

    @Override
    public String type() {
        return JAVA_OBJECT_TYPE;
    }

    @Override
    public Object unWrap(Envelope envelope) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
