/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;

/**
 * An object with partially changeable meta. Inner layer of meta is not
 * accessible via configure, but outer layer is a mutable Configuration. It also
 * supports value context and descriptor (by default descriptor is loaded from
 * the class).
 * <p>
 * <p>
 * Note that {@code getConfig()} method provides only mutable part</p>
 *
 * @author Alexander Nozik
 */
public abstract class BaseConfigurable extends SimpleConfigurable {

    private Laminate laminate;

    public BaseConfigurable() {
        laminate = new Laminate(getConfig()).withDescriptor(DescriptorUtils.buildDescriptor(getClass()));
    }

    /**
     * Set unconformable meta layers below configuration layer
     *
     * @param metaBase
     */
    protected final void setMetaBase(Meta... metaBase) {
        laminate = new Laminate(metaBase).withFirstLayer(getConfig()).withDescriptor(DescriptorUtils.buildDescriptor(getClass()));
    }

    protected final void setDescriptor(NodeDescriptor descriptor) {
        laminate = laminate.withDescriptor(descriptor);
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
    public Laminate getMeta() {
        return laminate;
    }

}
