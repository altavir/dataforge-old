package hep.dataforge.cache;

import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.workspace.identity.Identity;

import javax.cache.configuration.Configuration;

/**
 * Meta implementation of JCache configuration
 * Created by darksnake on 10-Feb-17.
 */
public class MetaCacheConfiguration<V> extends SimpleConfigurable implements Configuration<Identity,V> {

    private final Class<V> valueType;

    public MetaCacheConfiguration(hep.dataforge.meta.Configuration config, Class<V> valueType) {
        super(config);
        this.valueType = valueType;
    }

    @Override
    public Class<Identity> getKeyType() {
        return Identity.class;
    }

    @Override
    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public boolean isStoreByValue() {
        return true;
    }

}
