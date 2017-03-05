package hep.dataforge.meta;

/**
 * A meta node that is guaranteed to be immutable.
 * Created by darksnake on 05-Mar-17.
 */
public final class ImmutableMeta extends MetaNode<ImmutableMeta> {
    public ImmutableMeta(String name) {
        super(name);
    }
}
