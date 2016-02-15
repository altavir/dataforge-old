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
 * An object with partially changeable meta. Inner layer of meta is not
 * accessible via configure, but outer layer is a mutable Configuration. It also
 * supports value context and descriptor (by default descriptor is loaded from
 * the class).
 *
 * <p>
 * Note that {@code getConfig()} method provides only mutable part</p>
 *
 * @author Alexander Nozik
 */
public abstract class BaseConfigurable extends SimpleConfigurable {

    private final Laminate laminate;

    public BaseConfigurable() {
        laminate = new Laminate(getConfig()).setDescriptor(DescriptorUtils.buildDescriptor(getClass()));
    }
    
    protected final void setMetaBase(Meta... metaBase) {
        this.laminate.setLayers(metaBase);
        this.laminate.addFirstLayer(getConfig());        
    }

    protected final void setValueContext(ValueProvider context) {
        this.laminate.setDefaultValueProvider(context);
    }

    protected final void setDescriptor(NodeDescriptor descriptor) {
        this.laminate.setDescriptor(descriptor);
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

}
