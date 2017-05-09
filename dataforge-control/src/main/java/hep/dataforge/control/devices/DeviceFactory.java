package hep.dataforge.control.devices;

import hep.dataforge.context.Context;
import hep.dataforge.control.connections.Connection;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.ContextMetaFactory;

/**
 * Created by darksnake on 06-May-17.
 */
public interface DeviceFactory<D extends Device> extends ContextMetaFactory<D> {
    /**
     * The type of the device factory. One factory can supply multiple device classes depending on configuration.
     *
     * @return
     */
    String getType();

    /**
     * Default builder for connection with a given role for specified device
     *
     * @param role
     * @return
     */
    default Connection<D> buildConnection(String role, Context context, Meta meta) {
        //TODO add basic connections
        throw new RuntimeException("Connection with role " + role + " not supported by factory");
    }

//    /**
//     * Build the device and apply connections from configuration
//     * @param context
//     * @param meta
//     * @return
//     */
//    default D buildAndConnect(Context context, Meta meta){
//        D device = build(context, meta);
//        meta.getMetaList("connection").forEach(m->{
//
//        });
//    }
}
