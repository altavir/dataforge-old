/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace.identity;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;

import java.util.Objects;


public class ValueIdentity implements Identity {

    private Value val;

    public ValueIdentity(Object val) {
        this.val = Value.of(val);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.val);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValueIdentity other = (ValueIdentity) obj;
        return Objects.equals(this.val, other.val);
    }

    @Override
    public String toString() {
        return "string::" + val;
    }

    @Override
    public Meta toMeta() {
        return new MetaBuilder("id")
                .setValue("value", this.val);
    }

    @Override
    public void fromMeta(Meta meta) {
        this.val = meta.getValue("value");
    }
}
