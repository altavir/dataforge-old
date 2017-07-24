package hep.dataforge.utils;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;

/**
 * A simple metamorph implementation based on {@link MetaHolder}.
 * It is supposed, that there is no state fields beside meta itself
 * Created by darksnake on 20-Nov-16.
 */
public class SimpleMetaMorph extends MetaHolder implements MetaMorph {
    public SimpleMetaMorph(Meta meta) {
        super(meta);
    }

    public SimpleMetaMorph() {
        super();
    }

    @Override
    public Meta toMeta() {
        return meta();
    }

    @Override
    public void fromMeta(Meta meta) {
        setMeta(meta);
    }

    @Override
    public int hashCode() {
        return meta().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass()) && ((Metoid) obj).meta().equals(meta());
    }
}
