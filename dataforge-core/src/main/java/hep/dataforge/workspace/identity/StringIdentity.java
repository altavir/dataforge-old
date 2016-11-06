/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace.identity;

import java.util.Objects;


public class StringIdentity implements Identity {
    private final String str;

    public StringIdentity(String str) {
        this.str = str;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.str);
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
        final StringIdentity other = (StringIdentity) obj;
        return Objects.equals(this.str, other.str);
    }

    @Override
    public String toString() {
        return "string::" + str;
    }
}
