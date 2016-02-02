/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.navigation.ValueProvider;

/**
 * An object with partially changeable meta. Inner layer of meta is immutable,
 * but outer layer is a mutable Configuration. It also supports value context
 * and descriptor (by default descriptor is loaded from the class).
 *
 * <p>
 * Note that {@code getConfig()} method provides only mutable part</p>
 *
 * @author Alexander Nozik
 */
public abstract class BaseConfigurable extends SimpleConfigurable implements Annotated {

    private final Meta meta;
    private final Laminate laminate;

    public BaseConfigurable(Meta metaBase) {
        this.meta = metaBase;
        if (metaBase == null) {
            laminate = new Laminate(getConfig()).setDescriptor(DescriptorUtils.buildDescriptor(getClass()));
        } else {
            laminate = new Laminate(getConfig(), metaBase).setDescriptor(DescriptorUtils.buildDescriptor(getClass()));
        }
    }

    public BaseConfigurable(Meta metaBase, ValueProvider context) {
        this.meta = metaBase;
        laminate = new Laminate(getConfig(), metaBase)
                .setDefaultValueProvider(context)
                .setDescriptor(DescriptorUtils.buildDescriptor(getClass()));
    }

    public BaseConfigurable(Meta meta, ValueProvider context, NodeDescriptor descriptor) {
        this.meta = meta;
        laminate = new Laminate(getConfig(), meta)
                .setDefaultValueProvider(context)
                .setDescriptor(descriptor);
    }

    /**
     * Combined meta. Is not immutable!
     * <p>
     * The implementation is a {@code Laminate} so it is in general slower, than
     * {@code Configuration}.
     * </p>
     *
     * @return
     */
    @Override
    public Meta meta() {
        return laminate;
    }

    /**
     * Immutable meta base
     *
     * @return
     */
    public Meta getMetaBase() {
        return meta;
    }

}
