/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.names;

import hep.dataforge.description.Described;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import org.slf4j.LoggerFactory;

/**
 * The base class for {@code Meta} objects with immutable meta which also
 * implements ValueProvider and Described interfaces
 *
 * @author Alexander Nozik
 */
public class BaseMetaHolder implements Annotated, ValueProvider, Described {

    public static final Meta DEFAULT_EMPTY_META = Meta.empty();

    private Meta meta;
    private NodeDescriptor descriptor;

    public BaseMetaHolder(Meta meta) {
        this.meta = meta;
    }

    public BaseMetaHolder() {
    }
    
    

    /**
     * Return meta of this object. If it is null, than return default meta from
     * {@code getDefaultMeta()} method
     *
     * @return
     */
    @Override
    public Meta meta() {
        if (meta == null) {
            return getDefaultMeta();
        }
        return meta;
    }

    /**
     * The meta that should be used if defined meta is null
     *
     * @return
     */
    protected Meta getDefaultMeta() {
        return DEFAULT_EMPTY_META;
    }

    /**
     * The method to modify meta after creation. It could be blocked by
     * implementation
     *
     * @param meta
     */
    protected void setMeta(Meta meta) {
        if(meta != null){
            LoggerFactory.getLogger(getClass()).warn("Overriding meta of the Annotanted object");
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
            descriptor = Described.super.getDescriptor();
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

    /**
     * If this object's meta provides given value, return it, otherwise, use
     * descriptor
     *
     * @param name
     * @return
     */
    @Override
    public Value getValue(String name) {
        if (meta().hasValue(name)) {
            return meta().getValue(name);
        } else if (getDescriptor().hasDefaultForValue(name)) {
            return getDescriptor().valueDescriptor(name).defaultValue();
        } else {
            throw new NameNotFoundException(name);
        }
    }

    /**
     * true if this object's meta
     *
     * @param name
     * @return
     */
    @Override
    public boolean hasValue(String name) {
        return meta().hasValue(name) || getDescriptor().hasDefaultForValue(name);
    }

}
