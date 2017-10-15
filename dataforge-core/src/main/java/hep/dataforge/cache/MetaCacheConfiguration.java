package hep.dataforge.cache;

import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaHolder;

import javax.cache.configuration.Configuration;

/**
 * Meta implementation of JCache configuration
 * Created by darksnake on 10-Feb-17.
 */
public class MetaCacheConfiguration<V> extends MetaHolder implements Configuration<Meta,V> {

    private final Class<V> valueType;

    public MetaCacheConfiguration(Meta config, Class<V> valueType) {
        super(config);
        this.valueType = valueType;
    }

    @Override
    public Class<Meta> getKeyType() {
        return Meta.class;
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
