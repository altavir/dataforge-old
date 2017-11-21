/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.utils;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.meta.DescribedMetoid;
import hep.dataforge.meta.Meta;
import org.slf4j.LoggerFactory;

/**
 * The base class for {@code Meta} objects with immutable meta which also
 * implements ValueProvider and Described interfaces
 *
 * @author Alexander Nozik
 */
public class MetaHolder implements DescribedMetoid {

    private Meta meta;
    private transient NodeDescriptor descriptor;

    public MetaHolder(Meta meta) {
        this.meta = meta;
    }

    public MetaHolder() {
    }


    /**
     * Return meta of this object. If it is null, than return default meta from
     * {@code getDefaultMeta()} method
     *
     * @return
     */
    @Override
    public Meta getMeta() {
        if (meta == null) {
            return getDefaultMeta();
        }
        return meta;
    }

    /**
     * The meta that should be used if defined meta is not present
     *
     * @return
     */
    protected Meta getDefaultMeta() {
        return Meta.empty();
    }

    /**
     * The method to modify meta after creation. It could be blocked by
     * implementation
     *
     * @param meta
     */
    protected void setMeta(Meta meta) {
        if (this.meta != null) {
            LoggerFactory.getLogger(getClass()).warn("Overriding meta of the MetaHolder");
        }
        this.meta = meta;
    }

    /**
     * Get descriptor and cache it in case we will need it again
     *
     * @return
     */
    @Override
    public NodeDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = DescribedMetoid.super.getDescriptor();
        }
        return descriptor;
    }

    /**
     * Reserved method to set override descriptor later
     *
     * @param descriptor
     */
    protected final void setDescriptor(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
    }

}
