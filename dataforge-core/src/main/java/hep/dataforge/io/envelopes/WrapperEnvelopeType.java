/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

@Deprecated
public class WrapperEnvelopeType extends DefaultEnvelopeType {

    @Override
    public String description() {
        return "DataForge object serialization";
    }

    @Override
    public String getName() {
        return "df.wrapper";
    }

    @Override
    public int getCode() {
        return Wrappable.WRAPPER_ENVELOPE_CODE;
    }

    @Override
    public boolean infiniteMetaAllowed() {
        return false;
    }

    @Override
    public boolean infiniteDataAllowed() {
        return true;
    }
}
