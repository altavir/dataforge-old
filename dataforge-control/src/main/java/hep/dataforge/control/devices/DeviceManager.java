/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.context.PluginDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.NamedMetaFactory;
import hep.dataforge.utils.ReferenceRegistry;

/**
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "device", group = "hep.dataforge", description = "Basic DataForge storage plugin")
public class DeviceManager extends BasicPlugin implements Encapsulated {

    private ReferenceRegistry<Device> devices;
    //TODO move it to Basic plugin and check fro plugin reattachment
    private Context context;

    @Override
    public void apply(Context context) {
        this.context = context;
    }

    @Override
    public void clean(Context context) {
        this.context = null;
    }

    /**
     * Create and register new device instance using basic (name, context, meta)
     * constructor via reflects
     *
     * @param <T>
     * @param name
     * @param type
     * @param meta
     * @return
     */
    public <T extends Device> T createDevice(Class<T> type, String name, Meta meta) {
        try {
            T device = type.getConstructor(String.class, Context.class, Meta.class).newInstance(name, getContext(), meta);
            this.devices.add(device);
            return device;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends Device> T createDevice(NamedMetaFactory<T> factory, String name, Meta meta) {
        T device = factory.build(name, getContext(), meta);
        this.devices.add(device);
        return device;
    }
    
    //TODO method to automatically add connections from device meta
    
    public <T extends Device> T getDevice(String name, Class<T> type){
        return (T) devices.findFirst((d)-> type.isInstance(d) && (name == null || name.equals(d.getName()))).orElse(null);
    }
    

//    public <T extends Connection> T createConnection(Class<T> type, String name, Meta meta) {
//        try {
//            T conn = type.getConstructor(String.class, Context.class, Meta.class).newInstance(name, getContext(), meta);
//            return conn;
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//    
//
//
//    public <T extends Connection> T createConnection(NamedMetaFactory<T> factory, String name, Meta meta) {
//        return factory.build(name, getContext(), meta);
//    }

    @Override
    public Context getContext() {
        return this.context;
    }

}
